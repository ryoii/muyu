package cn.ryoii.muyu.ui;

import com.intellij.openapi.util.ScalableIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class RelativeImageIcon extends ImageIcon implements ScalableIcon {

    private final double originRatio;
    private float scale = 1.0f;

    public RelativeImageIcon(Image image) {
        super(image);
        originRatio = (double) image.getWidth(getImageObserver()) / image.getHeight(getImageObserver());
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        Point sp = new Point(0, 0);
        Dimension componentSize = c.getSize();
        Dimension imageSize = c.getSize();

        imageSize.width = ((int) (Math.min(componentSize.width, imageSize.width) * scale));
        imageSize.height = (int) (imageSize.width / originRatio);

        sp.x = (int) (c.getAlignmentX() * (componentSize.width - imageSize.width));
        sp.y = (int) (c.getAlignmentY() * (componentSize.height - imageSize.height));

        if (getImageObserver() == null) {
            g.drawImage(getImage(), sp.x, sp.y, imageSize.width, imageSize.height, c);
        } else {
            g.drawImage(getImage(), sp.x, sp.y, imageSize.width, imageSize.height, getImageObserver());
        }
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public @NotNull Icon scale(float scaleFactor) {
        this.scale = scaleFactor;
        return this;
    }
}
