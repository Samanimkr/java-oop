package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.Move.*;

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
		private int currentRound = 0;


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

			// Checking attributes are not null
			if (mrX == null) throw new NullPointerException();
			if (detectives == null) throw new NullPointerException();

			// Making sure that mrX is not a detective
			if (mrX.isDetective()) throw new IllegalArgumentException();

			ArrayList<Piece> coloursTaken = new ArrayList<>();
			ArrayList<Integer> locationsTaken = new ArrayList<>();

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
				if (detective.has(Ticket.SECRET)) throw new IllegalArgumentException();
				if (detective.has(Ticket.DOUBLE)) throw new IllegalArgumentException();
			}
			// Throw an exception if there are no rounds
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException();

			// A list of all the players (MrX and the detectives)
			this.everyone = ImmutableList.<Player>builder().add(this.mrX).addAll(this.detectives).build();

			// Calculate the possible single and double moves that the player can take and store it in 'moves'
			ImmutableSet<Move.SingleMove> singleMoves = makeSingleMoves(this.mrX, this.mrX.location());
			ImmutableSet<Move.DoubleMove> doubleMoves = makeDoubleMoves(this.mrX, this.mrX.location(), singleMoves);
			this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).addAll(doubleMoves).build();
		}

		private final class PlayerTickets implements TicketBoard {
			private final ImmutableMap<Ticket, Integer> tickets;

			private PlayerTickets(ImmutableMap<Ticket, Integer> tickets) {
				// Initialise the local tickets variable
				this.tickets = tickets;
			}

			@Override public int getCount(@Nonnull Ticket ticket) {
				// Return the count of the ticket or return 0 if the player doesn't have that ticket
				return this.tickets.getOrDefault(Objects.requireNonNull(ticket), 0);
			}
		}

		private ImmutableSet<SingleMove> makeSingleMoves(Player player, int source) {
			// Creating an array for the available singleMoves
			final ArrayList<SingleMove> singleMoves = new ArrayList<>();

			// Iterate through all the destinations the player could move to from his current position
			for (int destination : this.setup.graph.adjacentNodes(source)) {
				boolean occupied = false;

				// If the destination is occupied by a detective then set 'occupied' to true
				for (Player detective : this.detectives) {
					if (detective.location() == destination) {
						occupied = true;
						break;
					}
				}

				// Skip this iteration if the destination has been occupied
				if (occupied) continue;

				// Go through all the transport methods that can take the player to that destination
				for (Transport t : Objects.requireNonNull(this.setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
					// If the player has the required transport ticket then add this destination to avaiable singleMoves
					if (player.has(t.requiredTicket()))
						singleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
				}

				// If the player has a 'Secret' ticket then add another singleMoves destination using a 'Secret' ticket
				if (player.has(Ticket.SECRET)) {
					singleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
				}
			}
			// Return the set of single moves
			return ImmutableSet.copyOf(singleMoves);
		}

		private ImmutableSet<DoubleMove> makeDoubleMoves(Player player, int source, ImmutableSet<SingleMove> singleMoves) {
			int roundsLeft = this.setup.rounds.size() - currentRound;
			if (!player.has(Ticket.DOUBLE) || roundsLeft < 2) return ImmutableSet.of();

			// Creating an array for the available singleMoves
			final ArrayList<DoubleMove> doubleMoves = new ArrayList<>();

			for (var move : singleMoves) {
				var source2 = move.visit(new Visitor<Integer>() {
					@Override public Integer visit(SingleMove move) {
						return move.destination;
					}

					@Override
					public Integer visit(DoubleMove move) {
						return move.destination1;
					}
				});

				for (int destination : this.setup.graph.adjacentNodes(source2)) {
					boolean occupied = false;

					// If the destination is occupied by a detective then set 'occupied' to true
					for (Player detective : this.detectives) {
						if (detective.location() == destination) {
							occupied = true;
							break;
						}
					}

					// Skip this iteration if the destination has been occupied
					if (occupied) continue;

					// Go through all the transport methods that can take the player to that destination
					for (Transport t : Objects.requireNonNull(this.setup.graph.edgeValueOrDefault(source2, destination, ImmutableSet.of()))) {
						// If the player has the required transport ticket then add this destination to avaiable singleMoves
						if ((player.has(t.requiredTicket()) && t.requiredTicket() != move.ticket)
								|| (player.hasAtLeast(t.requiredTicket(),2) && t.requiredTicket() == move.ticket))
							doubleMoves.add(new DoubleMove(player.piece(), source, move.ticket, source2, t.requiredTicket(), destination));
					}

					// If the player has 2 'Secret' tickets then add another doubemove destination using a 'Secret' ticket
					if (player.hasAtLeast(Ticket.SECRET, 2)) {
						doubleMoves.add(new DoubleMove(player.piece(), source, move.ticket, source2, Ticket.SECRET, destination));
					}
				}
			}

			return ImmutableSet.copyOf(doubleMoves);
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
			for (final Player p : detectives) {
				if (p.piece() == detective) return Optional.of(p.location());
			}
			// If it wasn't found then return empty
			return Optional.empty();
		};
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			// Find the piece we're looking for and return their ticket as a PlayerTickets object
			for (final Player p : this.everyone) {
				if (p.piece() == piece) return Optional.of(new PlayerTickets(p.tickets()));
			};
			// If it wasn't found then return empty
			return Optional.empty();
		};
		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return this.log; };
		@Override public ImmutableSet<Move> getAvailableMoves() { return this.moves; };
		@Override public GameState advance(Move move) {
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
			if (move.commencedBy().isDetective()) currentRound++;

			return null;
		};
		@Override public ImmutableSet<Piece> getWinner() { return ImmutableSet.of(); };
	}

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		// Check the graph is not empty
		if (setup.graph.edges().size() == 0) throw new IllegalArgumentException();

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
