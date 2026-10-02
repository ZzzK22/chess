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
    public boolean hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        hasMoved = false;
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

    private Collection<ChessMove> bishopAndRookAndQueenMoves(ChessBoard board, ChessPosition pos){
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(pos);

        for (int i = 0; i < 9; i++) {
            int drow = i % 3 - 1;
            int dcol = i / 3 - 1;
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
        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition pos){
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(pos);

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
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition pos){
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(pos);

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
        // castling
        if(!piece.hasMoved){ // has the king moved?
            int homeRow = piece.pieceColor == ChessGame.TeamColor.WHITE ? 1 : 8;
            ChessPosition rookInColumn1 = new ChessPosition(homeRow, 1);
            ChessPosition column2 = new ChessPosition(homeRow, 2);
            ChessPosition column3 = new ChessPosition(homeRow, 3);
            ChessPosition column4 = new ChessPosition(homeRow, 4);
            // pos is column 5
            ChessPosition column6 = new ChessPosition(homeRow, 6);
            ChessPosition column7 = new ChessPosition(homeRow, 7);
            ChessPosition rookInColumn8 = new ChessPosition(homeRow, 8);
            if(board.getPiece(rookInColumn8) != null && !board.getPiece(rookInColumn8).hasMoved && // has the rook moved?
                    board.getPiece(column6) == null && board.getPiece(column7) == null // is the row empty?
            ){
                moves.add(new ChessMove(pos, column7, null));
            }
            if(board.getPiece(rookInColumn1) != null && !board.getPiece(rookInColumn1).hasMoved && // has the rook moved?
                    board.getPiece(column2) == null && board.getPiece(column3) == null && board.getPiece(column4) == null // is the row empty?
            ){
                moves.add(new ChessMove(pos, column3, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition pos){
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece piece = board.getPiece(pos);

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
        for(var dcol : new int[]{-1, 1}) {
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
            if(pos.getRow() == (piece.pieceColor == ChessGame.TeamColor.WHITE ? 5 : 4) &&
                    board.getPreviousMove() != null &&
                    Math.abs(board.getPreviousMove().getStartPosition().getColumn() - pos.getColumn()) == 1 &&
                    board.getPiece(board.getPreviousMove().getEndPosition()).getPieceType() == PieceType.PAWN &&
                    Math.abs(board.getPreviousMove().getStartPosition().getRow() - board.getPreviousMove().getEndPosition().getRow()) == 2){
                moves.add(new ChessMove(pos, target, null));
            }
        }
        return moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        ChessPiece piece = board.getPiece(pos);
        switch(piece.type){
            case PieceType.QUEEN: case PieceType.BISHOP: case PieceType.ROOK:
                return bishopAndRookAndQueenMoves(board, pos);
            case PieceType.KING:
                return kingMoves(board, pos);
            case PieceType.KNIGHT:
                return knightMoves(board, pos);
            case PieceType.PAWN:
                return pawnMoves(board, pos);
        }
        return new ArrayList<ChessMove>();
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

    @Override
    public String toString() {
        char piece = 0;
        switch(type) {
            case PAWN -> piece += 'P';
            case KING -> piece += 'K';
            case ROOK -> piece += 'R';
            case QUEEN -> piece += 'Q';
            case BISHOP -> piece += 'B';
            case KNIGHT -> piece += 'N';
        }
        return String.valueOf((char)(piece + (pieceColor == ChessGame.TeamColor.BLACK ? ' ' : '\0')));
    }
}
