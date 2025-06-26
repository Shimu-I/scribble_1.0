package com.example.scribble;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PuzzlePiece {
    private double targetX, targetY;
    private double currentX, currentY;
    private double width, height;
    private Image image;
    private Rectangle shape;
    private boolean snapped;
    private int col, row;

    public PuzzlePiece(double targetX, double targetY, double width, double height, Image image, Rectangle shape, int col, int row) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.currentX = targetX;
        this.currentY = targetY;
        this.width = width;
        this.height = height;
        this.image = image;
        this.shape = shape;
        this.snapped = false;
        this.col = col;
        this.row = row;
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.translate(currentX, currentY);

        // Draw the background and clip area
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        gc.beginPath();
        gc.rect(0, 0, width, height);
        gc.clip();

        // Draw the image
        gc.drawImage(image, col * width, row * height, width, height, 0, 0, width, height);

        // Apply the stroke after clipping to ensure visibility
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); // Increased from 1 to make it more visible
        gc.strokeRect(0, 0, width, height);

        gc.restore();
    }

    public boolean contains(double x, double y) {
        if (shape == null) {
            System.err.println("Invalid shape in contains check for piece at (" + col + ", " + row + ")");
            return false;
        }
        return x >= currentX && x <= currentX + width && y >= currentY && y <= currentY + height;
    }

    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public void setCurrentX(double x) { this.currentX = x; }
    public void setCurrentY(double y) { this.currentY = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isSnapped() { return snapped; }
    public void setSnapped(boolean snapped) { this.snapped = snapped; }
}