package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private boolean isWhitesTurn;


    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        isWhitesTurn = true;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return isWhitesTurn ? TeamColor.WHITE : TeamColor.BLACK;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        isWhitesTurn = team == TeamColor.WHITE;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){
            return new ArrayList<>();
        }
        ArrayList<ChessMove> moves = new ArrayList<>(piece.pieceMoves(board, startPosition));

        for (int i = 0; i < moves.size(); i++) {
            ChessMove move = moves.get(i);
            if(board.isInCheckWithThisMove(board.getPiece(startPosition).getTeamColor(), move)){
                moves.remove(move);
                i--;
                continue;
            }
            if(board.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.KING &&
                    Math.abs(startPosition.getColumn() - move.getEndPosition().getColumn()) == 2){
                ChessMove moveOnTheWay = new ChessMove(startPosition, new ChessPosition(startPosition.getRow(),
                        (startPosition.getColumn() + move.getEndPosition().getColumn()) / 2), null);
                if(board.isInCheck(board.getPiece(startPosition).getTeamColor()) ||
                        board.isInCheckWithThisMove(board.getPiece(startPosition).getTeamColor(), moveOnTheWay)){
                    moves.remove(move);
                    i--;
                }
            }
        }

        return moves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(validMoves(move.startPosition).contains(move) &&
                board.getPiece(move.getStartPosition()).getTeamColor() == getTeamTurn()){
            if(board.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                    move.getEndPosition().getColumn() != move.getStartPosition().getColumn() &&
                    board.getPiece(move.getEndPosition()) == null){ // en passant
                board.removePiece(new ChessPosition(move.startPosition.getRow(), move.endPosition.getColumn()));
            }
            board.movePiece(move);
            board.getPiece(move.getEndPosition()).hasMoved = true;
            if(board.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.KING){
                if(move.getStartPosition().getColumn() - move.getEndPosition().getColumn() == 2) {
                    board.movePiece(new ChessMove(new ChessPosition(move.endPosition.getRow(), 1),
                            new ChessPosition(move.endPosition.getRow(), 4), null));
                } else if(move.getStartPosition().getColumn() - move.getEndPosition().getColumn() == -2) {
                    board.movePiece(new ChessMove(new ChessPosition(move.endPosition.getRow(), 8),
                            new ChessPosition(move.endPosition.getRow(), 6), null));
                }
            }
            board.setPreviousMove(move);
            isWhitesTurn = !isWhitesTurn;
        } else {
            throw new InvalidMoveException("Invalid move");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return board.isInCheck(teamColor);
    }

    private boolean isInMate(TeamColor teamColor){
        for(int row = 1; row < 9; row++){
            for(int col = 1; col < 9; col++){
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if(piece != null && piece.getTeamColor() == teamColor && !validMoves(pos).isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInMate(teamColor) && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return isInMate(teamColor) && !isInCheck(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return isWhitesTurn == chessGame.isWhitesTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, isWhitesTurn);
    }
}
