package game2Dupdates.entitites;

import java.awt.*;

public class GameButton {

    private Rectangle bounds;
    private String label;
    private boolean hovered;

    //Color
    private Color baseColor ;
    private Color hoverColor ;
    private Color textColor ;

    public GameButton(String label,int x,int y,int width,int height,Color baseColor,Color hoverColor,Color textColor) {
        this.label =  label;
        this.bounds = new Rectangle(x,y,width,height);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
    }

    public void draw(Graphics2D g) {
        // Buttonbackground
        g.setColor(baseColor);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

        //Border
        g.setColor(baseColor);
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

        //Centered label
        g.setColor(hovered? hoverColor:textColor);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
        int textY = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2;
        g.drawString(label, textX, textY);
    }

    public void setHovered(int mouseX,int mouseY) {
        hovered = bounds.contains(mouseX,mouseY);
    }

    public boolean isClicked(int mouseX, int mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    public void setPosition(int x, int y) {
        bounds.setLocation(x, y);
    }
}
