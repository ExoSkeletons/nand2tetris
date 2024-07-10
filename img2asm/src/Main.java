import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
	public static final String
			QUICK_FILE_PATH = "img.png",
			OUTPUT_PATH = "output.asm";
	private static final String
			COLOR_V = "color",
			_DRAW_COLOR = "DRAW_COLOR",
			CALL_RETURN_A = "callReturnA",
			CLEAR_CALL_RETURN_A = "clearCallReturnA";
	public static final int
			SCREEN_W = 512, SCREEN_H = 256,
			SCREEN_A = 16384,
			BIT_L = 16,
			SCREEN_REG_N = SCREEN_W / BIT_L * SCREEN_H;

	public static boolean DO_PRE_CLEAR_WITH_CUSTOM_CLEAR = false;

	public static void main(String[] args) {
		System.out.println("Hello Hack!");

		System.out.print("Checking for file " + QUICK_FILE_PATH + "... ");
		File imgFile = new File(QUICK_FILE_PATH);
		if (imgFile.exists()) {
			System.out.println("found!");
			try {
				writeGeneratedFile(ImageIO.read(imgFile));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else System.out.println("fnf.");

		startImageChooserApplet();
	}

	public static void writeGeneratedFile(BufferedImage image) {
		try {
			String asmDrawCode = generateHackAsmDrawCode(image);
			System.out.println(asmDrawCode);
			Files.writeString(Path.of(OUTPUT_PATH), asmDrawCode);
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
			throw new IllegalArgumentException(
					"Invalid image size.\n" +
							"Image must be " + SCREEN_W + "x" + SCREEN_H + "," +
							" got " + image.getWidth() + "x" + image.getHeight() + "."
			);

		byte[] bytes = Tools.imgToBytes(image);

		StringBuilder asmText = new StringBuilder();

		asmText.append("// --=  draw image (generated) =--\n\n");

		asmText.append("(DRAW_IMAGE)\n");

		//*
		// clear screen
		// find most used bit (0 / -1), for clearing
		if (DO_PRE_CLEAR_WITH_CUSTOM_CLEAR)
			asmText
					.append("// ** clearing screen, with plain bytes (whites) **\n")
					.append("// set fill color to white color\n")
					.append("@" + COLOR_V + "\n")
					.append("M = 0\n")
					.append("// set call return to here\n")
					.append("@" + CLEAR_CALL_RETURN_A + "\n")
					.append("D = A\n")
					.append("@" + CALL_RETURN_A + "\n")
					.append("M = D\n")
					.append("// call draw\n")
					.append("@" + _DRAW_COLOR + "\n")
					.append("0 ; JMP\n")
					.append("(" + CLEAR_CALL_RETURN_A + ")\n")
					;
		//**/
		asmText.append("\n");
		asmText.append("// ** draw non-background bytes\n");
		for (int i = 0; i < SCREEN_REG_N; i++) {
			byte b = bytes[i];
			if (b != 0) {
				if (b == -1 || b == 1)
					asmText
							.append("@").append(i + SCREEN_A).append("\n")
							.append("M = ").append(b).append("\n");
				else {
					if (b > 0)
						asmText
								.append("@").append(b).append("\n")
								.append("D = A\n");
					else
						asmText
								.append("@").append(-b).append("\n")
								.append("D = A\n")
								.append("D = - D\n");
					asmText
							.append("@").append(i + SCREEN_A).append("\n")
							.append("M = D\n");
				}
			}
		}
		asmText.append("\n");
		// end
		asmText.append("// ** end **\n");
		asmText
				.append("(DRAW_IMAGE_END)\n")
				.append("@DRAW_IMAGE_END\n")
				.append("0 ; JMP\n")
		;
		return asmText.toString();
	}




}