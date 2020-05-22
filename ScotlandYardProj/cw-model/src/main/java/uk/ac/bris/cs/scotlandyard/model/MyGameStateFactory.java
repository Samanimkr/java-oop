package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives
		) {
			// initialise the local attributes
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			this.everyone = ImmutableList.<Player>builder().add(this.mrX).addAll(this.detectives).build();

			// Checking attributes are not null
			if (mrX == null) throw new NullPointerException();
			if (mrX.isDetective()) throw new IllegalArgumentException();
			if (detectives == null) throw new NullPointerException();

			ArrayList<Piece> coloursTaken = new ArrayList<Piece>();
			ArrayList<Integer> locationsTaken = new ArrayList<Integer>();

			for (Player detective : detectives) {
				if (detective == null) throw new NullPointerException();
				if (detective.isMrX()) throw new IllegalArgumentException();

				if (coloursTaken.contains(detective.piece())) throw new IllegalArgumentException(); // testDuplicateDetectivesShouldThrow
				coloursTaken.add(detective.piece());

				if (locationsTaken.contains(detective.location())) throw new IllegalArgumentException(); // testLocationOverlapBetweenDetectivesShouldThrow
				locationsTaken.add(detective.location());

				if (detective.has(ScotlandYard.Ticket.SECRET)) throw new IllegalArgumentException(); // testDetectiveHaveSecretTicketShouldThrow
				if (detective.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException(); // testDetectiveHaveSecretTicketShouldThrow
			}
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException();
		}

		@Override public GameSetup getSetup() { return setup; };

		@Override public ImmutableSet<Piece> getPlayers() {
			return ImmutableSet.<Piece>copyOf(
					this.everyone.stream().map(d -> d.piece()).collect(Collectors.toSet())
			);
		};

		@Override public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (final var p : detectives) {
				if (p.piece() == detective) return Optional.of(p.location());
			}
			return Optional.empty();
		};

		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for (final var p : this.everyone.asList()) {
				if (p.piece() == piece) return Optional.of(new Tickets(p.tickets()));
			};
			return Optional.empty();
		};

		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return this.log; };
		@Override public ImmutableSet<Piece> getWinner() { return ImmutableSet.of(); };
		@Override public ImmutableSet<Move> getAvailableMoves() { return null; };
		@Override public GameState advance(Move move) { return null; };


		private final class Tickets implements TicketBoard {
			private final ImmutableMap<ScotlandYard.Ticket, Integer> tickets;

			private Tickets(ImmutableMap<ScotlandYard.Ticket, Integer> tickets) {
				this.tickets = tickets;
			}

			@Override public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
				return this.tickets.getOrDefault(Objects.requireNonNull(ticket), 0);
			}
		}

	}

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		if (setup.graph.edges().size() == 0) throw new IllegalArgumentException();

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
