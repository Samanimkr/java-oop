package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;

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
			// Initialising the local attributes
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.everyone = ImmutableList.<Player>builder().add(this.mrX).addAll(this.detectives).build();

			// Checking attributes are not null
			if (mrX == null) throw new NullPointerException();
			if (detectives == null) throw new NullPointerException();

			// Making sure that mrX is not a detective
			if (mrX.isDetective()) throw new IllegalArgumentException();

			ArrayList<Piece> coloursTaken = new ArrayList<Piece>();
			ArrayList<Integer> locationsTaken = new ArrayList<Integer>();

			for (Player detective : detectives) {
				// Making sure the detective isn't null and that it is not a MrX piece
				if (detective == null) throw new NullPointerException();
				if (detective.isMrX()) throw new IllegalArgumentException();

				// No duplicate detectives in the game
				if (coloursTaken.contains(detective.piece())) throw new IllegalArgumentException();
				coloursTaken.add(detective.piece());

				// No two detectives should have the same location
				if (locationsTaken.contains(detective.location())) throw new IllegalArgumentException();
				locationsTaken.add(detective.location());

				// Detectives shouldn't have a 'Secret' or 'Double' ticket
				if (detective.has(ScotlandYard.Ticket.SECRET)) throw new IllegalArgumentException();
				if (detective.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException();
			}
			// Throw an exception if there are no rounds
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException();
		}

		private final class PlayerTickets implements TicketBoard {
			private final ImmutableMap<ScotlandYard.Ticket, Integer> tickets;

			private PlayerTickets(ImmutableMap<ScotlandYard.Ticket, Integer> tickets) {
				this.tickets = tickets;
			}

			@Override public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
				// Return the count of the ticket or return 0 if the player doesn't have that ticket
				return this.tickets.getOrDefault(Objects.requireNonNull(ticket), 0);
			}
		}

		@Override public GameSetup getSetup() { return setup; };
		@Override public ImmutableSet<Piece> getPlayers() {
			// Go through all the players and return a set of their pieces
			return ImmutableSet.<Piece>copyOf(
					this.everyone.stream().map(d -> d.piece()).collect(Collectors.toSet())
			);
		};
		@Override public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			// Find the detective we're looking for and return their location
			for (final var p : detectives) {
				if (p.piece() == detective) return Optional.of(p.location());
			}
			// If it wasn't found then return empty
			return Optional.empty();
		};
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			// Find the piece we're looking for and return their ticket as a PlayerTickets object
			for (final var p : this.everyone) {
				if (p.piece() == piece) return Optional.of(new PlayerTickets(p.tickets()));
			};
			// If it wasn't found then return empty
			return Optional.empty();
		};
		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return this.log; };
		@Override public ImmutableSet<Piece> getWinner() { return ImmutableSet.of(); };
		@Override public ImmutableSet<Move> getAvailableMoves() { return null; };
		@Override public GameState advance(Move move) { return null; };
	}

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		// Check the graph is not empty
		if (setup.graph.edges().size() == 0) throw new IllegalArgumentException();

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
