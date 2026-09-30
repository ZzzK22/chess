package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BinaryOperator;

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
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){
            return moves;
        }
        return piece.pieceMoves(board, startPosition);

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
            // make move
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
        ChessPosition kingPos = null;
        for(int row = 1; row < 9; row++){
            for(int col = 1; col < 9; col++) {
                var pos = new ChessPosition(row, col);
                var piece = board.getPiece(pos);
                if(piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingPos = new ChessPosition(row, col);
                    row = 10;
                    break;
                }
            }
        }
        if(kingPos == null) return false; // if we failed to find the king, you're obviously not in check
        for(int row = 1; row < 9; row++){
            for(int col = 1; col < 9; col++){
                var pos = new ChessPosition(row, col);
                var piece = board.getPiece(pos);
                if(piece == null || piece.getTeamColor() == teamColor){
                    continue;
                }
                var moves = piece.pieceMoves(board, pos);
                for(ChessMove move : moves){
                    if(move.getEndPosition() == kingPos){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
