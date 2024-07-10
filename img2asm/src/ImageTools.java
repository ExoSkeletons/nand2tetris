import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.*;

public class ImageTools {
	public static BufferedImage scaleImg(BufferedImage img, int newWidth, int newHeight) {
		BufferedImage resized = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(
				img,
				0, 0, newWidth, newHeight, 0, 0,
				img.getWidth(), img.getHeight(), null
		);
		g.dispose();
		return resized;
	}

	/**
	 * Returns the supplied src image brightened by a float value from 0 to 10.
	 * Float values below 1.0f actually darken the source image.
	 */
	public static BufferedImage brighten(BufferedImage src, float level) {
		BufferedImage dst = new BufferedImage(
				src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		float[] scales = {level, level, level};
		float[] offsets = new float[4];
		RescaleOp rop = new RescaleOp(scales, offsets, null);

		Graphics2D g = dst.createGraphics();
		g.drawImage(src, rop, 0, 0);
		g.dispose();

		return dst;
	}

	public static BufferedImage imageIconToBufferedImage(ImageIcon imageIcon) {
		// paint the Icon to the BufferedImage.
		BufferedImage bufferedImage = new BufferedImage(
				imageIcon.getIconWidth(), imageIcon.getIconHeight(),
				BufferedImage.TYPE_INT_RGB
		);
		Graphics g = bufferedImage.createGraphics();
		imageIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		return bufferedImage;
	}

	public static BufferedImage bw(BufferedImage image) {
		BufferedImage binaryImage = new BufferedImage(
				image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY
		);
		Graphics g = binaryImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return binaryImage;
	}
}
