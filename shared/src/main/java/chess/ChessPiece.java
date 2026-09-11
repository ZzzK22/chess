package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(pos);

        switch(piece.type){
            case PieceType.BISHOP:
                for (var drow : new int[]{-1, 1})
                    for (var dcol : new int[]{-1, 1}){
                        int row = pos.getRow() + drow;
                        int col = pos.getColumn() + dcol;
                        while(row < 9 && row > 0 && col < 9 && col > 0) {
                            var target = new ChessPosition(row, col);
                            if (board.getPiece(target) == null) {
                                moves.add(new ChessMove(pos, target, null));
                                row += drow;
                                col += dcol;
                                continue;
                            }
                            if(board.getPiece(target).pieceColor != pieceColor){
                                moves.add(new ChessMove(pos, target, null));
                            }
                            break;
                        }
                    }
                break;
            case PieceType.QUEEN:
                for (var drow : new int[]{-1, 0, 1})
                    for (var dcol : new int[]{-1, 0, 1}){
                        if(drow == dcol && drow == 0) continue;
                        int row = pos.getRow() + drow;
                        int col = pos.getColumn() + dcol;
                        while(row < 9 && row > 0 && col < 9 && col > 0) {
                            var target = new ChessPosition(row, col);
                            if (board.getPiece(target) == null) {
                                moves.add(new ChessMove(pos, target, null));
                                row += drow;
                                col += dcol;
                                continue;
                            }
                            if(board.getPiece(target).pieceColor != pieceColor){
                                moves.add(new ChessMove(pos, target, null));
                            }
                            break;
                        }
                    }
                break;
            case PieceType.ROOK:
                for (var drow : new int[]{-1, 0, 1})
                    for (var dcol : new int[]{-1, 0, 1}){
                        if((drow + dcol) % 2 == 0) continue;
                        int row = pos.getRow() + drow;
                        int col = pos.getColumn() + dcol;
                        while(row < 9 && row > 0 && col < 9 && col > 0) {
                            var target = new ChessPosition(row, col);
                            if (board.getPiece(target) == null) {
                                moves.add(new ChessMove(pos, target, null));
                                row += drow;
                                col += dcol;
                                continue;
                            }
                            if(board.getPiece(target).pieceColor != pieceColor){
                                moves.add(new ChessMove(pos, target, null));
                            }
                            break;
                        }
                    }
                break;
            case PieceType.KING:
                for (var row : new int[]{-1, 0, 1})
                    for (var col : new int[]{-1, 0, 1}) {
                        var target = new ChessPosition(pos.getRow() + row, pos.getColumn() + col);
                        boolean targetOutOfBounds = pos.getRow() + row < 0 || pos.getRow() + row > 7 ||
                                                 pos.getColumn() + col < 0 || pos.getColumn() + col > 7;
                        if(targetOutOfBounds || (board.getPiece(target) != null && board.getPiece(target).getTeamColor() == getTeamColor()))
                            continue;
                        else
                            moves.add(new ChessMove(pos, target, null));
                    }
        }

        return moves;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || o.getClass() != getClass())
            return false;
        ChessPiece that = (ChessPiece) o;
        return that.type == type && that.pieceColor == pieceColor;
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(type) + (pieceColor == ChessGame.TeamColor.WHITE ? 1 : 0);
    }
}
