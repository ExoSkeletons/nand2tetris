import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
	public static final String QUICK_FILE_PATH = "img.png", OUTPUT_PATH = "output.asm";
	private static final String COLOR_V = "color", _DRAW_COLOR = "DRAW_COLOR", CALL_RETURN_A = "callReturnA", CLEAR_CALL_RETURN_A = "clearCallReturnA";
	public static boolean DO_PRE_CLEAR = true, USE_CUSTOM_CLEAR = false;
	public static byte B_WHITE = 0, B_BLACK = -1;

	public static void main(String[] args) {
		System.out.println("Hello Hack!");

		String inputPath = QUICK_FILE_PATH;
		boolean inputPathSpecifed = false;
		if (args.length >= 1) {
			inputPathSpecifed = true;
			inputPath = args[0];
		}
		String outputPath = (args.length >= 2) ? args[1] : OUTPUT_PATH;

		System.out.print("Looking for " + (inputPathSpecifed ? "specified" : "quick") + " file " + inputPath + "... ");
		if (Files.exists(Path.of(inputPath))) {
			BufferedImage img;
			try {
				img = ImageIO.read(new File(inputPath));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			writeGeneratedFile(img, outputPath);
		} else {
			System.out.println("File not found.");
			if (!inputPathSpecifed)
				startImageChooserApplet();
		}
	}

	public static void writeGeneratedFile(BufferedImage image, String outputPath) {
		try {
			String asmDrawCode = generateHackAsmDrawCode(image);
			// System.out.println(asmDrawCode);
			Files.writeString(Path.of(outputPath), asmDrawCode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void startImageChooserApplet() {
		System.out.println("Starting file chooser applet...");
		JFrame frame = new ChooserAppFrame();
		frame.setVisible(true);
	}

	private static String generateHackAsmDrawCode(BufferedImage image) {
		System.out.println("Generating...");

		if (image.getWidth() != SCREEN_W || image.getHeight() != SCREEN_H)
			throw new IllegalArgumentException("Invalid image size.\n" + "Image must be " + SCREEN_W + "x" + SCREEN_H + "," + " got " + image.getWidth() + "x" + image.getHeight() + ".");

		byte[] bytes = Tools.imgToBytes(image);

		StringBuilder asmText = new StringBuilder();

		asmText.append("// --=  draw image (generated) =--\n\n");

		asmText.append("(DRAW_IMAGE)\n\n");

		// determine and mark most used color (b/w) as bg
		int bc = 0, wc = 0;
		for (byte b : bytes)
			if (b == B_WHITE) // 000...0
				wc++;
			else if (b == B_BLACK) // 111...1
				bc++;
		boolean bgWhite = wc >= bc;
		int clearC = bgWhite ? B_WHITE : B_BLACK;
		if (DO_PRE_CLEAR) { // TODO: replace with in-house fill code
			// clear screen
			asmText.append("// ** clearing screen, with plain bytes (").append(bgWhite ? "white" : "black").append(") **\n").append("(PRE_DRAW_CLEAR)\n");
			if (USE_CUSTOM_CLEAR)
				asmText.append("// set fill color to bg color\n").append("@" + COLOR_V + "\n").append("M = ").append(clearC).append("\n").append("// set call return to here\n").append("@" + CLEAR_CALL_RETURN_A + "\n").append("D = A\n").append("@" + CALL_RETURN_A + "\n").append("M = D\n").append("// call draw\n").append("@" + _DRAW_COLOR + "\n").append("0 ; JMP\n").append("(" + CLEAR_CALL_RETURN_A + ")\n");
			else asmText.append("@i\n").append("M = 0\n")

					.append("(CLEAR_SCREEN_LOOP)\n")

					.append("@").append(SCREEN_REG_N).append("\n").append("D = A\n").append("@i\n").append("D = D - M\n").append("@CLEAR_SCREEN_LOOP_EXIT\n").append("D;JEQ\n")

					.append("@").append(SCREEN_A).append("\n").append("D = A\n").append("@i\n").append("A = M + D\n").append("M = ").append(clearC).append("\n").append("@i\n").append("M = M + 1\n").append("@CLEAR_SCREEN_LOOP\n").append("0;JMP\n")

					.append("(CLEAR_SCREEN_LOOP_EXIT)\n");
			asmText.append("\n");
		}
		//**/
		asmText.append("\n");
		asmText.append("// ** draw (non-background) bytes\n");
		for (int i = 0; i < SCREEN_REG_N; i++) {
			byte b = bytes[i];
			if (!DO_PRE_CLEAR || b != clearC) {
				if (b == -1 || b == 1 || b == 0)
					// set with immediate
					asmText.append("@").append(i + SCREEN_A).append("\n").append("M = ").append(b).append("\n");
				else {
					// set using A register
					asmText.append("@").append(b < 0 ? -b : b).append("\n").append("D = ").append(b < 0 ? "-A" : "A").append("\n");
					asmText.append("@").append(i + SCREEN_A).append("\n").append("M = D\n");
				}
			}
		}
		asmText.append("\n");
		// end
		asmText.append("// ** end **\n");
		asmText.append("(DRAW_IMAGE_END)\n").append("@DRAW_IMAGE_END\n").append("0 ; JMP\n");
		return asmText.toString();
	}

	public static final int
			SCREEN_W = 512, SCREEN_H = 256, SCREEN_A = 16384,
			BIT_L = 16,
			SCREEN_REG_N = SCREEN_W / BIT_L * SCREEN_H;
}