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
            case PieceType.QUEEN: case PieceType.BISHOP: case PieceType.ROOK:
                for (int i = 0; i < 9; i++) {
                    int drow = i % 3 - 1; // this stuff is nasty. makes sense to me,
                    int dcol = i / 3 - 1; // but the autograder was worried about too much nesting
                    if (piece.type == PieceType.ROOK && (drow + dcol) % 2 == 0 ||
                        piece.type == PieceType.BISHOP && (drow == 0 || dcol == 0) ||
                     /* piece.type == PieceType.QUEEN && */ drow == 0 && dcol == 0) {
                        continue;
                    } // the code up to here effectively gets the correct directions for the pieces.
                    int row = pos.getRow() + drow;
                    int col = pos.getColumn() + dcol;
                    while (row < 9 && row > 0 && col < 9 && col > 0) {
                        var target = new ChessPosition(row, col);
                        if (board.getPiece(target) == null) {
                            moves.add(new ChessMove(pos, target, null));
                            row += drow;
                            col += dcol;
                            continue;
                        }
                        if (board.getPiece(target).pieceColor != pieceColor) {
                            moves.add(new ChessMove(pos, target, null));
                        }
                        break;
                    }
                }
                break;
            case PieceType.KING:
                for (var row : new int[]{-1, 0, 1}) {
                    for (var col : new int[]{-1, 0, 1}) {
                        var target = new ChessPosition(pos.getRow() + row, pos.getColumn() + col);
                        boolean targetOutOfBounds = pos.getRow() + row < 1 || pos.getRow() + row > 8 ||
                                pos.getColumn() + col < 1 || pos.getColumn() + col > 8;
                        if (targetOutOfBounds || (board.getPiece(target) != null && board.getPiece(target).getTeamColor() == getTeamColor())) {
                            continue;
                        } else {
                            moves.add(new ChessMove(pos, target, null));
                        }
                    }
                }
                break;
            case PieceType.KNIGHT:
                for (var row : new int[]{-2, -1, 0, 1, 2}) {
                    for (var col : new int[]{-2, -1, 0, 1, 2}) {
                        if (row * row + col * col != 5) {
                            continue;
                        }
                        var target = new ChessPosition(pos.getRow() + row, pos.getColumn() + col);
                        boolean targetOutOfBounds = pos.getRow() + row < 1 || pos.getRow() + row > 8 ||
                                pos.getColumn() + col < 1 || pos.getColumn() + col > 8;
                        if (targetOutOfBounds || (board.getPiece(target) != null && board.getPiece(target).getTeamColor() == getTeamColor())) {
                            continue;
                        } else {
                            moves.add(new ChessMove(pos, target, null));
                        }
                    }
                }
                break;
            case PieceType.PAWN:
                int direction = piece.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
                var target = new ChessPosition(pos.getRow() + direction, pos.getColumn());
                boolean promote = pos.getRow() + direction == 1 || pos.getRow() + direction == 8;
                if (board.getPiece(target) == null) {
                    if(promote){
                        moves.add(new ChessMove(pos, target, PieceType.QUEEN));
                        moves.add(new ChessMove(pos, target, PieceType.ROOK));
                        moves.add(new ChessMove(pos, target, PieceType.BISHOP));
                        moves.add(new ChessMove(pos, target, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(pos, target, null));
                        target = new ChessPosition(pos.getRow() + 2 * direction, pos.getColumn());
                        if (pos.getRow() == (piece.pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7) && board.getPiece(target) == null) {
                            moves.add(new ChessMove(pos, target, null));
                        }
                    }
                }
                for(var dcol : new int[]{-1, 1}){
                    if (pos.getColumn() + dcol == 0 || pos.getColumn() + dcol == 9) {
                        continue;
                    }
                    target = new ChessPosition(pos.getRow() + direction, pos.getColumn() + dcol);
                    if (board.getPiece(target) != null && board.getPiece(target).getTeamColor() != piece.pieceColor) {
                        if (promote) {
                            moves.add(new ChessMove(pos, target, PieceType.KNIGHT));
                            moves.add(new ChessMove(pos, target, PieceType.BISHOP));
                            moves.add(new ChessMove(pos, target, PieceType.ROOK));
                            moves.add(new ChessMove(pos, target, PieceType.QUEEN));
                        } else {
                            moves.add(new ChessMove(pos, target, null));
                        }
                    }
                }
        }

        return moves;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) {
            return true;
        }
        if(o == null || o.getClass() != getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return that.type == type && that.pieceColor == pieceColor;
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(type) + (pieceColor == ChessGame.TeamColor.WHITE ? 1 : 0);
    }
}
