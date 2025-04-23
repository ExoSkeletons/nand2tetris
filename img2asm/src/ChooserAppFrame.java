import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ChooserAppFrame extends JFrame {
	final JLabel imageLabel = new JLabel(), bwIL = new JLabel(), scaledBWIL = new JLabel();
	final ChangeListener sliderCL;
	final JFileChooser chooser = new JFileChooser();
	final JButton output = new JButton("GENERATE .asm CODE");
	BufferedImage ogImage;

	ChooserAppFrame() {
		imageLabel.setSize(70, 70);
		bwIL.setPreferredSize(imageLabel.getSize());
		scaledBWIL.setSize(Main.SCREEN_W, Main.SCREEN_H);

		JSlider brightenSlider = new JSlider();
		sliderCL = e -> {
			ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();

			boolean imageLoaded = imageIcon != null && imageIcon.getImage() != null;
			brightenSlider.setVisible(imageLoaded);
			output.setVisible(imageLoaded);

			if (imageLoaded) {
				float brightenValue = brightenSlider.getValue() / 10f;

				BufferedImage iconImage = ImageTools.imageIconToBufferedImage(imageIcon);
				BufferedImage bwImage = ImageTools.bw(ImageTools.brighten(iconImage, brightenValue));
				BufferedImage outputImage = ImageTools.bw(
						ImageTools.brighten(
								ImageTools.scaleImg(ogImage, Main.SCREEN_W, Main.SCREEN_H),
								brightenValue
						)
				); // have the output images use the og image, and not double-scale.

				imageLabel.setSize(iconImage.getWidth(), iconImage.getHeight());

				bwIL.setIcon(new ImageIcon(bwImage));
				bwIL.setSize(bwImage.getWidth(), bwImage.getHeight());

				scaledBWIL.setIcon(new ImageIcon(outputImage));

				setSize(imageLabel.getWidth() * 2 + scaledBWIL.getWidth(), getHeight());
			}
		};
		brightenSlider.addChangeListener(sliderCL);
		sliderCL.stateChanged(new ChangeEvent(this));

		JButton fileButton = new JButton("choose file");
		fileButton.addActionListener(e -> {
			chooser.setDialogTitle("Choose Image");
			chooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));

			openChooser(chooser);
		});

		output.addActionListener(e -> {
			Main.writeGeneratedFile(ImageTools.imageIconToBufferedImage((ImageIcon) scaledBWIL.getIcon()), Main.OUTPUT_PATH);
			try {
				Desktop.getDesktop().edit(new File(Main.OUTPUT_PATH));
			} catch (IOException ignored) {
			}
		});

		JPanel left = new JPanel(new GridLayout(2, 1));
		left.add(imageLabel);
		left.add(fileButton);
		add(left, BorderLayout.WEST);

		JPanel center = new JPanel(new GridLayout(2, 1));
		center.add(bwIL);
		center.add(brightenSlider);
		add(center, BorderLayout.CENTER);

		JPanel right = new JPanel(new GridLayout(2, 1));
		right.add(scaledBWIL);
		right.add(output);
		add(right, BorderLayout.EAST);

		setTitle("Hack image2asm");
		setMinimumSize(new Dimension(Main.SCREEN_W * 2, Main.SCREEN_H));
		setSize(500, 500);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private void openChooser(JFileChooser chooser) {
		int r = chooser.showOpenDialog(this);

		if (r == JFileChooser.APPROVE_OPTION) {
			File imgFile = chooser.getSelectedFile();
			try {
				ogImage = ImageIO.read(imgFile);
				// update image
				float scaleFactor = imageLabel.getHeight() / (float) ogImage.getHeight();
				int newW = (int) (ogImage.getWidth() * scaleFactor);
				imageLabel.setIcon(new ImageIcon(ImageTools.scaleImg(
						ogImage,
						newW, imageLabel.getHeight()
				)));
				// call image update
				sliderCL.stateChanged(new ChangeEvent(this));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
