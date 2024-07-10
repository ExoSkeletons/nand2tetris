import java.awt.*;
import java.awt.image.BufferedImage;

public class Tools {
	static byte[] imgToBytes(BufferedImage image) {
		// draw image as binary
		BufferedImage binary = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = binary.createGraphics();
		g.drawImage(image, 0, 0, null);

		// extract bytes
		boolean[] buffer = new boolean[Main.BIT_L];
		byte[] bytes = new byte[image.getHeight() * image.getWidth() / Main.BIT_L];
		int i = 0;
		for (int y = 0; y < image.getHeight(); y++, i++)
			for (int x = 0; x + Main.BIT_L < image.getWidth(); x += Main.BIT_L, i++) {
				for (int bi = 0; bi < Main.BIT_L; bi++)
					buffer[bi] = image.getRGB(x + bi, y) != -1; // 111111111 = white
				bytes[i] = (byte) booleansToInt(buffer, Main.BIT_L);
			}
		return bytes;
	}

	public static int booleansToInt(boolean[] arr, int length) {
		int n = 0, len = Math.min(arr.length, length);
		for (int i = len - 1; i >= 0; i--) {
			boolean b = arr[i];
			n = (n << 1) | (b ? 1 : 0);
		}
		return n;
	}
}
