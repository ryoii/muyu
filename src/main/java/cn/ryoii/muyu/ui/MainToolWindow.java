package cn.ryoii.muyu.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ImageLoader;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class MainToolWindow implements ToolWindowFactory, DumbAware {
    private static final int EXPECT_FRAME_COUNT = 20;
    private static final float EXPECT_SCALE = 0.975F;
    private static final int ANIMATION_TIME_MS = 50;

    protected JPanel mainPanel;

    // north components
    private JSlider durationSlider;
    private JLabel delayLabel;

    // center components
    private JPanel animationPanel;
    private JLabel pic;
    private RelativeImageIcon icon;
    private Clip clip;

    // south components
    private JCheckBox autoCheckBox;
    private JCheckBox muteCheckBox;
    private JLabel counterLabel;

    private long count = 0;

    private int frameCount = EXPECT_FRAME_COUNT;
    private final Timer animationTimer = new Timer(ANIMATION_TIME_MS / EXPECT_FRAME_COUNT, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (frameCount > 0) {
                // scale from 1.0 to 'EXPECT_SCALE'
                float scale = EXPECT_SCALE - (1 - EXPECT_SCALE) * frameCount / EXPECT_FRAME_COUNT;
                icon.scale(scale);
                animationPanel.repaint();
                frameCount--;
            } else {
                animationTimer.stop();
                frameCount = EXPECT_FRAME_COUNT;
            }
        }
    });

    // default 1 second
    private final Timer autoTimer = new Timer(1000, e -> zoomPic());

    public MainToolWindow() {
        autoCheckBox.addItemListener(this::listenAutoCheckBox);
        durationSlider.addChangeListener(this::listenSlider);

        autoTimer.setDelay(durationSlider.getValue());
        delayLabel.setText(durationSlider.getValue() + "ms");
    }

    private void createUIComponents() {
        Image image = ImageLoader.loadFromResource("/muyu.png", getClass());

        icon = new RelativeImageIcon(image);
        pic = new JLabel(icon);

        pic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (autoCheckBox.isSelected()) return;
                zoomPic();
            }
        });

        try(InputStream is = getClass().getResourceAsStream("/hit.wav")) {
            if (is == null) {
                throw new RuntimeException("cannot read hit.wav");
            }
            try(BufferedInputStream bis = new BufferedInputStream(is)) {
                clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(bis));
            }
        } catch (Exception e) {
            throw new RuntimeException("cannot read hit.wav", e);
        }
    }

    private void listenAutoCheckBox(ItemEvent event) {
        boolean autoHit = event.getStateChange() == ItemEvent.SELECTED;
        if (autoHit) {
            autoTimer.start();
        } else {
            autoTimer.stop();
        }
    }

    private void listenSlider(ChangeEvent event) {
        JSlider slider = (JSlider) event.getSource();
        autoTimer.setDelay(slider.getValue());
        delayLabel.setText(slider.getValue() +"ms");
    }

    private void zoomPic() {
        animationTimer.restart();
        counterLabel.setText(String.valueOf(++count));
        if (!muteCheckBox.isSelected()) {
            clip.loop(1);
        }
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.getContentManager().addContent(
                ContentFactory.SERVICE.getInstance().createContent(mainPanel, "", false)
        );
    }
}
