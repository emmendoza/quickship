package dev_t.cs161.quickship;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.abs;

public class quickShipViewPlayModePlayerGrid extends View {

    private Point screen = new Point();
    private volatile boolean held;
    private volatile Float initialX, initialY;
    private volatile Float endX, endY;
    private Float screenWidth;
    private Float screenHeight;
    private Float swipeThreshold;
    private Paint boardGridFramePaint;
    private Paint boatHitPaint;
    private Paint boatMissPaint;
    private Float boardGridFrameStartX;
    private Float boardGridFrameStartY;
    private Float boardGridFrameEndX;
    private Float boardGridFrameEndY;
    private Float boardGridCellWidth;
    private Float boardGridCellHeight;
    private Paint boardGridLinePaint;
    private Float boardGridLinePaintStrokeWidth;
    private Float boardGridFrameDividerX[];
    private Float boardGridFrameDividerY[];
    private int currentIndex;
    private int selectedIndex;
    private Paint boardGridSelectedPaint;
    private Float boardGridSelectedStartX;
    private Float boardGridSelectedStartY;
    private Float boardGridSelectedEndX;
    private Float boardGridSelectedEndY;
    private Float boardGridFrameMargin;
    private Float viewWidth;
    private Float viewHeight;
    private Paint titlePaint;
    private String mTitle;
    private Float mTitleWidth;
    private Float mTitleHeight;
    private Float mTitleX;
    private Float mTitleY;
    private quickShipModel mGameModel;
    private quickShipActivityMain mMainActivity;
    private Rect hitSquare;
    private float[] hitXY;
    private float[] missXY;
    private Paint emojiPaint;
    private Paint mPlacedShipPaint;




    public quickShipViewPlayModePlayerGrid(quickShipActivityMain context, quickShipModel gameModel) {
        super(context);
        mMainActivity = context;
        mGameModel = gameModel;
        Display display = context.getWindowManager().getDefaultDisplay();
        display.getSize(screen);
        initializeValues();
        calculateBoardGUIPositions();
    }

    public void initializeValues() {
        mTitle = getContext().getResources().getString(R.string.play_mode_grid_player_title);
        held = true;
        currentIndex = -1;

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(16 * getResources().getDisplayMetrics().density);

        boardGridFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardGridFramePaint.setStyle(Paint.Style.FILL);
        boardGridFramePaint.setColor(ContextCompat.getColor(mMainActivity, R.color.play_mode_player_grid));

        boatHitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boatHitPaint.setStyle(Paint.Style.FILL);
        boatHitPaint.setColor(ContextCompat.getColor(mMainActivity, R.color.play_mode_player_ship_hit));

        boatMissPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boatMissPaint.setStyle(Paint.Style.FILL);
        boatMissPaint.setColor(ContextCompat.getColor(mMainActivity, R.color.play_mode_player_ship_miss));

        boardGridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardGridLinePaint.setStyle(Paint.Style.STROKE);
        int dpSize =  2;
        DisplayMetrics dm = mMainActivity.getResources().getDisplayMetrics() ;
        boardGridLinePaintStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        boardGridLinePaint.setStrokeWidth(boardGridLinePaintStrokeWidth);
        boardGridLinePaint.setColor(ContextCompat.getColor(mMainActivity, R.color.play_mode_player_grid_line));

        boardGridSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boardGridSelectedPaint.setStyle(Paint.Style.FILL);
        boardGridSelectedPaint.setColor(ContextCompat.getColor(mMainActivity, R.color.play_mode_player_cell_selected));
        boardGridFrameDividerX = new Float[11];
        boardGridFrameDividerY = new Float[11];

        emojiPaint = new Paint();
        emojiPaint.setStyle(Paint.Style.FILL);
        emojiPaint.setColor(Color.BLACK);
        emojiPaint.setTextAlign(Paint.Align.LEFT);

        mPlacedShipPaint = new Paint();
        mPlacedShipPaint.setAntiAlias(true);
        mPlacedShipPaint.setFilterBitmap(true);
        mPlacedShipPaint.setDither(true);
    }

    public void setGameModel(quickShipModel playerBoardData) {
        mGameModel = playerBoardData;
    }

    public void calculateBoardGUIPositions() {
        screenWidth = (float) screen.x;
        screenHeight = (float) screen.y;

        swipeThreshold = screenWidth * 0.2f;

        boardGridFrameMargin = (screenWidth - (screenWidth * (float) 0.9)) / 2;

        mTitleHeight = titlePaint.getTextSize();
        mTitleWidth = titlePaint.measureText(mTitle);
        mTitleX = boardGridFrameMargin;
        mTitleY = mTitleHeight + (mTitleHeight / 2);

        boardGridFrameStartX = boardGridFrameMargin;
        boardGridFrameStartY = boardGridFrameMargin + mTitleHeight;
        boardGridFrameEndX = boardGridFrameMargin + (screenWidth * (float) 0.9);
        boardGridFrameEndY = boardGridFrameMargin + (screenWidth * (float) 0.9);
        float boardGridFrameWidth = boardGridFrameEndX - boardGridFrameStartX;
        float boardGridFrameHeight = boardGridFrameEndY - boardGridFrameStartY;
        boardGridCellWidth = boardGridFrameWidth / 10;
        boardGridCellHeight = boardGridFrameHeight / 10;

        hitSquare = new Rect();

        viewWidth = screenWidth;
        viewHeight = boardGridFrameEndY + boardGridFrameMargin;

        float tempDividerX = boardGridFrameStartX;
        for (int i = 0; i < 11; i++) {
            boardGridFrameDividerX[i] = tempDividerX;
            tempDividerX = tempDividerX + boardGridCellWidth;
        }
        float tempDividerY = boardGridFrameStartY;
        for (int i = 0; i < 11; i++) {
            boardGridFrameDividerY[i] = tempDividerY;
            tempDividerY = tempDividerY + boardGridCellHeight;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mTitle, mTitleX, mTitleY, titlePaint);
        //canvas.drawRect(boardGridFrameStartX, boardGridFrameStartY, boardGridFrameEndX, boardGridFrameEndY, boardGridFramePaint);

        float verticalX = boardGridFrameStartX + boardGridCellWidth;
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(verticalX, boardGridFrameStartY, verticalX, boardGridFrameEndY, boardGridLinePaint);
            verticalX = verticalX + boardGridCellWidth;
        }
        float verticalY = boardGridFrameStartY + boardGridCellHeight;
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(boardGridFrameStartX, verticalY, boardGridFrameEndX, verticalY, boardGridLinePaint);
            verticalY = verticalY + boardGridCellHeight;
        }

        // Color hit and missed boxes properly
        for (int i = 0; i < 100; i++) {
            if (mGameModel.getPlayerGameBoard().isHit(i) && mGameModel.getPlayerGameBoard().isOccupied(i)) {
                hitXY = getIndexXYCoord(i);
                canvas.drawRect(hitXY[0], hitXY[1], hitXY[2], hitXY[3], boatHitPaint);
                canvas.drawLine(hitXY[0], hitXY[1], hitXY[2], hitXY[1], boardGridLinePaint);
                canvas.drawLine(hitXY[0], hitXY[1], hitXY[0], hitXY[3], boardGridLinePaint);
                canvas.drawLine(hitXY[2], hitXY[1], hitXY[2], hitXY[3], boardGridLinePaint);
                canvas.drawLine(hitXY[0], hitXY[3], hitXY[2], hitXY[3], boardGridLinePaint);
            }
            else if (mGameModel.getPlayerGameBoard().isHit(i) && !mGameModel.getPlayerGameBoard().isOccupied(i)) {
                missXY = getIndexXYCoord(i);
                canvas.drawRect(missXY[0], missXY[1], missXY[2], missXY[3], boatMissPaint);
                canvas.drawLine(missXY[0], missXY[1], missXY[2], missXY[1], boardGridLinePaint);
                canvas.drawLine(missXY[0], missXY[1], missXY[0], missXY[3], boardGridLinePaint);
                canvas.drawLine(missXY[2], missXY[1], missXY[2], missXY[3], boardGridLinePaint);
                canvas.drawLine(missXY[0], missXY[3], missXY[2], missXY[3], boardGridLinePaint);
            }
        }

        // Draw a ship over what was drawn before this
        for (int i = 0; i < 100; i++) {
            if (mGameModel.getPlayerGameBoard().isAnchor(i)) {
                quickShipModelBoardSlot anchorShip = mGameModel.getPlayerGameBoard().getShipSlotAtIndex(i);
                Bitmap tempBitmap = getGenerateBitmap(anchorShip.getShipType(), anchorShip.getOrientation());
                float[] tempXYcoord = getIndexXYCanvasBox(anchorShip.getAnchorIndex(), anchorShip.getShipType(), anchorShip.getOrientation());
                canvas.drawBitmap(tempBitmap, null, new RectF(tempXYcoord[0], tempXYcoord[1], tempXYcoord[2], tempXYcoord[3]), mPlacedShipPaint);
            }
        }

        // Draw a hit emoji icon over any ship spot that got hit
        for (int i = 0; i < 100; i++) {
            if (mGameModel.getPlayerGameBoard().isHit(i) && mGameModel.getPlayerGameBoard().isOccupied(i)) {
                hitXY = getIndexXYCoord(i);
                String emoji = mGameModel.getPlayerGameBoard().getSlotEmoji(i);
                Bitmap emojiBitmap = mMainActivity.textToBitmap(emoji, boardGridCellWidth);
                hitSquare.set(Math.round(hitXY[0]), Math.round(hitXY[1]), Math.round(hitXY[2]), Math.round(hitXY[3]));
                canvas.drawBitmap(emojiBitmap, null, hitSquare, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mMainActivity.getAnimating()) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();
                    held = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    endX = event.getX();
                    endY = event.getY();
                    if (initialX > endX && abs(initialX - endX) > swipeThreshold && !mMainActivity.getFireButtonPressed()) {
                        mMainActivity.playModeSwitchToOpponentGrid(null);
                    } else if (abs(initialX - endX) > swipeThreshold && !mMainActivity.getFireButtonPressed()) {
                        mMainActivity.playModeSwitchToPlayerGrid(null);
                    }
                    held = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    break;
                default:
            }
        }
        return true;
    }

    public Bitmap getGenerateBitmap(int shipType, int orientation) {
        int tempHeight;
        int tempWidth;
        Bitmap returnBitmap = null;
        if (orientation == quickShipModelBoardSlot.VERTICAL) {
            switch (shipType) {
                case quickShipModelBoardSlot.TWO:
                    tempHeight = Math.round(2 * boardGridCellHeight);
                    tempWidth = Math.round(boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size2_vertical, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.THREE_A:
                    tempHeight = Math.round(3 * boardGridCellHeight);
                    tempWidth = Math.round(boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size3_a_vertical, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.THREE_B:
                    tempHeight = Math.round(3 * boardGridCellHeight);
                    tempWidth = Math.round(boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size3_b_vertical, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.FOUR:
                    tempHeight = Math.round(4 * boardGridCellHeight);
                    tempWidth = Math.round(boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size4_vertical, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.FIVE:
                    tempHeight = Math.round(5 * boardGridCellHeight);
                    tempWidth = Math.round(boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size5_vertical, tempHeight, tempWidth);
                    break;
            }
        } else {
            switch (shipType) {
                case quickShipModelBoardSlot.TWO:
                    tempHeight = Math.round(boardGridCellHeight);
                    tempWidth = Math.round(2 * boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size2_horizontal, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.THREE_A:
                    tempHeight = Math.round(boardGridCellHeight);
                    tempWidth = Math.round(3 * boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size3_a_horizontal, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.THREE_B:
                    tempHeight = Math.round(boardGridCellHeight);
                    tempWidth = Math.round(3 * boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size3_b_horizontal, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.FOUR:
                    tempHeight = Math.round(boardGridCellHeight);
                    tempWidth = Math.round(4 * boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size4_horizontal, tempHeight, tempWidth);
                    break;

                case quickShipModelBoardSlot.FIVE:
                    tempHeight = Math.round(boardGridCellHeight);
                    tempWidth = Math.round(5 * boardGridCellHeight);
                    returnBitmap = mMainActivity.scaleDownDrawableImage(R.drawable.ship_size5_horizontal, tempHeight, tempWidth);
                    break;
            }
        }
        return returnBitmap;
    }

    // Returns an array where array[0] = startingX, array[1] = startingY, array[2] = endingX, array[4] = endingY
    public float[] getIndexXYCanvasBox(int index, int shipType, int orientation) {
        int xIndex = index % 10;
        index = index / 10;
        int yIndex = index % 10;
        float[] returnArray = new float[4];
        returnArray[0] = boardGridFrameDividerX[xIndex];
        returnArray[1] = boardGridFrameDividerY[yIndex];
        if (orientation == quickShipModelBoardSlot.VERTICAL) {
            switch (shipType) {
                case quickShipModelBoardSlot.TWO:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 1];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 2];
                    break;

                case quickShipModelBoardSlot.THREE_A:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 1];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 3];
                    break;

                case quickShipModelBoardSlot.THREE_B:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 1];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 3];
                    break;

                case quickShipModelBoardSlot.FOUR:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 1];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 4];
                    break;

                case quickShipModelBoardSlot.FIVE:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 1];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 5];
                    break;
            }
        } else {
            switch (shipType) {
                case quickShipModelBoardSlot.TWO:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 2];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 1];
                    break;

                case quickShipModelBoardSlot.THREE_A:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 3];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 1];
                    break;

                case quickShipModelBoardSlot.THREE_B:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 3];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 1];
                    break;

                case quickShipModelBoardSlot.FOUR:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 4];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 1];
                    break;

                case quickShipModelBoardSlot.FIVE:
                    returnArray[2] = boardGridFrameDividerX[xIndex + 5];
                    returnArray[3] = boardGridFrameDividerY[yIndex + 1];
                    break;
            }
        }
        return returnArray;
    }

    public void calculateSelectedRect(int index) {
        int xIndex = index % 10;
        index = index / 10;
        int yIndex = index % 10;
        boardGridSelectedStartX = boardGridFrameDividerX[xIndex];
        boardGridSelectedEndX = boardGridFrameDividerX[xIndex + 1];
        boardGridSelectedStartY = boardGridFrameDividerY[yIndex];
        boardGridSelectedEndY = boardGridFrameDividerY[yIndex + 1];
    }

    // Returns an array where array[0] = x, array[1] = y, array[2] = (bottom right) x, array[3] = (bottom right) y
    public float[] getIndexXYCoord(int index) {
        int xIndex = index % 10;
        index = index / 10;
        int yIndex = index % 10;
        float[] returnArray = new float[5];
        returnArray[0] = boardGridFrameDividerX[xIndex];
        returnArray[1] = boardGridFrameDividerY[yIndex];
        returnArray[2] = boardGridFrameDividerX[xIndex + 1];
        returnArray[3] = boardGridFrameDividerY[yIndex + 1];
        returnArray[4] = boardGridCellWidth;
        return returnArray;
    }

    public int calculateCellTouched(float x, float y) {
        int index = 0;
        for (int i = 0; i < 10; i++) {
            if (isBetween(y, boardGridFrameDividerY[i], boardGridFrameDividerY[i + 1])) {
                for (int j = 0; j < 10; j++) {
                    if (isBetween(x, boardGridFrameDividerX[j], boardGridFrameDividerX[j + 1])) {
                        return index;
                    }
                    index++;
                }
            }
            index = index + 10;
        }
        return index;
    }

    public float getViewHeight() {
        return viewHeight;
    }

    public float getViewWidth() {
        return viewWidth;
    }

    public float getBoardGridFrameMargin() {
        return boardGridFrameMargin;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void deSelectCell() {
        currentIndex = -1;
        boardGridSelectedStartX = null;
        boardGridSelectedStartY = null;
        boardGridSelectedEndX = null;
        boardGridSelectedEndY = null;
    }

    public static boolean isBetween(float x, float lower, float upper) {
        return lower <= x && x < upper;
    }

    public boolean insideBoardGridBound(float x, float y) {
        if (x < boardGridFrameStartX || x > boardGridFrameEndX || y < boardGridFrameStartY || y > boardGridFrameEndY) {
            return false;
        }
        else {
            return true;
        }
    }

    public void renderEmoji(String emojiString, float desiredWidth, float x, float y, Canvas canvas) {
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        emojiPaint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        emojiPaint.getTextBounds(emojiString, 0, emojiString.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        emojiPaint.setTextSize(desiredTextSize - 2);
        canvas.drawText(emojiString, x+1, y - (1.4f*(emojiPaint.ascent()+emojiPaint.descent())), emojiPaint);
    }

    public float getCellWidth() {
        return boardGridCellWidth;
    }
}