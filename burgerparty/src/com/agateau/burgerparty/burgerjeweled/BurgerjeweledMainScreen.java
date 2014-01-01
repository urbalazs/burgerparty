package com.agateau.burgerparty.burgerjeweled;

import java.util.HashSet;

import com.agateau.burgerparty.utils.MaskedDrawable;
import com.agateau.burgerparty.utils.Signal1;
import com.agateau.burgerparty.utils.SpriteImagePool;
import com.agateau.burgerparty.utils.StageScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

public class BurgerjeweledMainScreen extends StageScreen {
	private static final int SCORE_NORMAL = 200;
	private static final float BOARD_CELL_WIDTH = 100;
	private static final float BOARD_CELL_HEIGHT = 60;

	private class OurInputListener extends InputListener {
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			if (mBoard.hasDyingPieces()) {
				return false;
			}
			int col = MathUtils.floor(x / BOARD_CELL_WIDTH);
			int row = MathUtils.floor(y / BOARD_CELL_HEIGHT);
			mTouchedCol = col;
			mTouchedRow = row;
			return true;
		}

		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			Gdx.app.log("BJ", "touchUp");
			int col = MathUtils.floor(x / BOARD_CELL_WIDTH);
			int row = MathUtils.floor(y / BOARD_CELL_HEIGHT);
			if (col == mTouchedCol && row != mTouchedRow) {
				// vertical swipe
				Gdx.app.log("touchUp", "vswipe");
				mFirstPieceCol = mTouchedCol;
				mFirstPieceRow = mTouchedRow;
				swapPieces(col, mTouchedRow + (row > mTouchedRow ? 1 : -1));
				return;
			}
			if (col != mTouchedCol && row == mTouchedRow) {
				// horizontal swipe
				Gdx.app.log("touchUp", "hswipe");
				mFirstPieceCol = mTouchedCol;
				mFirstPieceRow = mTouchedRow;
				swapPieces(mTouchedCol + (col > mTouchedCol ? 1 : -1), row);
				return;
			}
			if (mFirstPieceCol == -1) {
				// first piece clicked
				Gdx.app.log("touchUp", "click1");
				mFirstPieceCol = col;
				mFirstPieceRow = row;
			} else {
				// second piece clicked
				Gdx.app.log("touchUp", "click2");
				swapPieces(col, row);
			}
		}

		private int mTouchedCol = -1;
		private int mTouchedRow = -1;
	}

	public BurgerjeweledMainScreen(BurgerjeweledMiniGame miniGame) {
		super(miniGame.getAssets().getSkin());
		mMiniGame = miniGame;
		createPool();
		createPieceDrawables();
		//createBg();
		resetBoard();
		createHud();
		getStage().addListener(new OurInputListener());
	}

	@Override
	public void render(float delta) {
		mTime += delta;
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		if (mGameOverDelay < 0) {
			getStage().act(delta);
			if (!mBoard.hasDyingPieces()) {
				findMatches();
			}
			if (!mBoard.hasDyingPieces() && mCollapseNeeded) {
				collapse();
			}
			getStage().draw();
		} else {
			mGameOverDelay += delta;
			/*if (mGameOverDelay > 2) {
				mMiniGame.showStartScreen();
			}*/
			getStage().draw();
		}
	}

	private void swapPieces(int col2, int row2) {
		assert(!mBoard.hasDyingPieces());
		Gdx.app.log("swapPieces", "mFirstPieceCol=" + mFirstPieceCol + " mFirstPieceRow=" + mFirstPieceRow + " col2=" + col2 + " row2=" + row2);
		assert(mFirstPieceCol != -1);
		assert(mFirstPieceRow != -1);
		int dc = Math.abs(mFirstPieceCol - col2);
		int dr = Math.abs(mFirstPieceRow - row2);
		boolean adjacentSwap = (dc == 1 && dr == 0) || (dc == 0 && dr == 1);
		if (!adjacentSwap) {
			Gdx.app.log("swapPieces", "not adjacent");
			mFirstPieceCol = -1;
			mFirstPieceRow = -1;
			return;
		}
		Piece piece1 = mBoard.getPiece(mFirstPieceCol, mFirstPieceRow);
		Piece piece2 = mBoard.getPiece(col2, row2);

		mPendingBoard.initFrom(mBoard);
		mPendingBoard.swap(mFirstPieceCol, mFirstPieceRow, col2, row2);
		if (mPendingBoard.hasMatchesAt(mFirstPieceCol, mFirstPieceRow) || mPendingBoard.hasMatchesAt(col2, row2)) {
			Board tmp = mPendingBoard;
			mPendingBoard = mBoard;
			mBoard = tmp;
			piece1.moveTo(piece2.getX(), piece2.getY());
			piece2.moveTo(piece1.getX(), piece1.getY());
		} else {
			Gdx.app.log("swap", "cancel swap");
			piece1.swapTo(piece2.getX(), piece2.getY());
			piece2.swapTo(piece1.getX(), piece1.getY());
		}
		mFirstPieceCol = -1;
		mFirstPieceRow = -1;
	}

	private void checkGameOver() {
		/*
		for (Enemy enemy: mEnemies) {
			if (enemy == null) {
				continue;
			}
			if (enemy.getY() < 0) {
				Gdx.app.log("Vaders", "Game Over");
				gameOver();
				break;
			}
		}
		*/
	}

	@Override
	public void onBackPressed() {
		mMiniGame.showStartScreen();
	}

	private void createBg() {
		TextureRegion region = mMiniGame.getAssets().getTextureAtlas().findRegion("levels/3/background");
		assert(region != null);
		setBackgroundActor(new Image(region));
	}

	private void createPieceDrawables() {
		TextureAtlas atlas = mMiniGame.getAssets().getTextureAtlas();
		mPiecesDrawable.add(new MaskedDrawable(atlas.findRegion("mealitems/0/top-inventory")));
		mPiecesDrawable.add(new MaskedDrawable(atlas.findRegion("mealitems/0/salad-inventory")));
		mPiecesDrawable.add(new MaskedDrawable(atlas.findRegion("mealitems/0/steak-inventory")));
		mPiecesDrawable.add(new MaskedDrawable(atlas.findRegion("mealitems/0/tomato-inventory")));
	}

	private void createPool() {
		mPool = new SpriteImagePool<Piece>(Piece.class);
		mPool.removalRequested.connect(mHandlers, new Signal1.Handler<Piece>() {
			@Override
			public void handle(Piece piece) {
				if (mBoard.removePiece(piece)) {
				}
			}
		});
	}

	private void resetBoard() {
		for (int row = 0; row < Board.BOARD_SIZE; ++row) {
			for (int col = 0; col < Board.BOARD_SIZE; ++col) {
				resetPiece(col, row);
			}
		}
	}

	private void resetPiece(int col, int row) {
		Piece piece = mPool.obtain();
		getStage().addActor(piece);
		int id = MathUtils.random(mPiecesDrawable.size - 1);
		float posX = col * BOARD_CELL_WIDTH;
		float posY = row * BOARD_CELL_HEIGHT; 
		piece.reset(mPiecesDrawable.get(id), id, posX, posY);
		mBoard.setPiece(col, row, piece);
	}

	private void findMatches() {
		findVerticalMatches();
		findHorizontalMatches();
		int score = 0;
		for (int row = 0; row < Board.BOARD_SIZE; ++row) {
			for (int col = 0; col < Board.BOARD_SIZE; ++col) {
				Piece piece = mBoard.getPiece(col, row);
				if (piece != null && piece.isMarked()) {
					piece.destroy();
					score += SCORE_NORMAL;
					mCollapseNeeded = true;
				}
			}
		}
		mScore += score;
		updateHud();
	}

	private void findVerticalMatches() {
		for (int col = 0; col < Board.BOARD_SIZE; ++col) {
			Array<Piece> column = mBoard.getColumn(col);
//			Gdx.app.log("findVerticalMatches", "col=" + col);
			int sameCount = 1;
			int lastId = -1;
			for (int row = 0; row < Board.BOARD_SIZE; ++row) {
				Piece piece = column.get(row);
				if (piece == null) {
					return;
				}
				if (piece.isDying()) {
					return;
				}
				int id = piece.getId();
//				Gdx.app.log("findVerticalMatches", "row=" + row + " id=" + id + " lastId=" + lastId + " sameCount=" + sameCount);
				if (id == lastId) {
					++sameCount;
				} else {
					lastId = id;
					if (sameCount >= 3) {
						deleteVerticalPieces(col, row - sameCount, sameCount);
					}
					sameCount = 1;
				}
			}
			if (sameCount >= 3) {
				deleteVerticalPieces(col, Board.BOARD_SIZE - sameCount, sameCount);
			}
		}
		//mGameOverDelay = 1;
	}

	private void deleteVerticalPieces(int col, int fromRow, int size) {
		Gdx.app.log("deleteVerticalPieces", "col=" + col + " row=" + fromRow + " size="+ size);
		for (int row = fromRow; row < fromRow + size; ++row) {
			mBoard.getPiece(col, row).mark();
		}
		mCollapseNeeded = true;
	}

	private void findHorizontalMatches() {
		for (int row = 0; row < Board.BOARD_SIZE; ++row) {
			int sameCount = 1;
			int lastId = -1;
			for (int col = 0; col < Board.BOARD_SIZE; ++col) {
				Piece piece = mBoard.getPiece(col, row);
				if (piece == null) {
					return;
				}
				if (piece.isDying()) {
					return;
				}
				int id = piece.getId();
				if (id == lastId) {
					++sameCount;
				} else {
					lastId = id;
					if (sameCount >= 3) {
						deleteHorizontalPieces(row, col - sameCount, sameCount);
					}
					sameCount = 1;
				}
			}
			if (sameCount >= 3) {
				deleteHorizontalPieces(row, Board.BOARD_SIZE - sameCount, sameCount);
			}
		}
	}

	private void deleteHorizontalPieces(int row, int fromCol, int size) {
		Gdx.app.log("deleteHorizontalPieces", "row=" + row + " col=" + fromCol + " size="+ size);
		for (int col = fromCol; col < fromCol + size; ++col) {
			mBoard.getPiece(col, row).mark();
		}
		mCollapseNeeded = true;
	}

	private void collapse() {
		if (mBoard.hasDyingPieces()) {
			Gdx.app.log("BJ", "collapsing canceled");
			return;
		}
		Gdx.app.log("BJ", "collapsing");
		mCollapseNeeded = false;
		for (int col = 0; col < Board.BOARD_SIZE; ++col) {
			collapseColumn(col);
		}
	}

	private void collapseColumn(int col) {
		Array<Piece> column = mBoard.getColumn(col);
		int fallSize = 0;
		int dstRow = 0;
		for (int row = 0; row < Board.BOARD_SIZE; ++row) {
			Piece piece = column.get(row);
			if (piece == null) {
				++fallSize;
			} else {
				if (fallSize > 0) {
					piece.fallTo(dstRow * BOARD_CELL_HEIGHT);
				}
				column.set(dstRow, piece);
				++dstRow;
			}
		}
		for (int row = Board.BOARD_SIZE - fallSize; row < Board.BOARD_SIZE; ++row) {
			resetPiece(col, row);
		}
	}

	private void createHud() {
		mScoreLabel = new Label("0", mMiniGame.getAssets().getSkin(), "score");
		getStage().addActor(mScoreLabel);
		mScoreLabel.setX(0);
		mScoreLabel.setY(getStage().getHeight() - mScoreLabel.getPrefHeight());
	}

	private void updateHud() {
		mScoreLabel.setText(String.valueOf((mScore / 10) * 10));
	}

	private void gameOver() {
		mGameOverDelay = 0;
	}

	private float mGameOverDelay = -1;
	private BurgerjeweledMiniGame mMiniGame;
	private int mScore = 0;
	private Label mScoreLabel;

	private SpriteImagePool<Piece> mPool;
	private float mTime = 0;

	private int mFirstPieceRow = -1;
	private int mFirstPieceCol = -1;
	private boolean mCollapseNeeded = false;
	private Board mBoard = new Board();
	private Board mPendingBoard = new Board();

	private Array<MaskedDrawable> mPiecesDrawable = new Array<MaskedDrawable>();
	private HashSet<Object> mHandlers = new HashSet<Object>();
}
