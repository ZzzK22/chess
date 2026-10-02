package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;

    public ChessMove getPreviousMove() {
        return previousMove;
    }

    public void setPreviousMove(ChessMove previousMove) {
        this.previousMove = previousMove;
    }

    private ChessMove previousMove;
    public ChessBoard() {
        board = new ChessPiece[8][8];
        previousMove = null;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }
    public void removePiece(ChessPosition position){
        addPiece(position, null);
    }
    /* Moves a piece on the chessboard.
     * Assumes the move is valid.
     */
    public void movePiece(ChessMove move){
        if(move.promotionPiece == null) {
            addPiece(move.getEndPosition(), getPiece(move.getStartPosition()));
        } else {
            addPiece(move.getEndPosition(), new ChessPiece(getPiece(move.getStartPosition()).getTeamColor(), move.promotionPiece));
        }
        removePiece(move.getStartPosition());
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for(var i = 2; i < 6; i++) {
            for(var j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }
        board[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        board[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        board[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        board[1][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        board[1][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);


        board[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        board[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        board[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

        board[6][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        board[6][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPos = null;
        for(int row = 1; row < 9; row++){
            for(int col = 1; col < 9; col++) {
                var pos = new ChessPosition(row, col);
                var piece = getPiece(pos);
                if(piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingPos = pos;
                    row = 10;
                    break;
                }
            }
        }
        if(kingPos == null) throw new RuntimeException("invalid state"); // if we failed to find the king, you're obviously not in check
        for(int row = 1; row < 9; row++){
            for(int col = 1; col < 9; col++){
                var pos = new ChessPosition(row, col);
                var piece = getPiece(pos);
                if(piece == null || piece.getTeamColor() == teamColor){
                    continue;
                }
                var moves = piece.pieceMoves(this, pos);
                for(ChessMove move : moves){
                    if(move.getEndPosition().equals(kingPos)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckWithThisMove(ChessGame.TeamColor teamColor, ChessMove move){
        ChessPiece eatenPiece = getPiece(move.getEndPosition());

        movePiece(move);

        boolean inCheck = isInCheck(getPiece(move.getEndPosition()).getTeamColor());

        ChessMove reverseMove = new ChessMove(move.getEndPosition(), move.getStartPosition(),
                move.promotionPiece != null ? ChessPiece.PieceType.PAWN : null // undo any promotions
        );
        movePiece(reverseMove);
        addPiece(move.getEndPosition(), eatenPiece);
        return inCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        return board.toString();
    }
}
