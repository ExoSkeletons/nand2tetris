import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;

public class Main {
	enum Jump {
		NULL, JGT, JEQ, JGE, JLT, JNE, JLE, JMP,
		;

		String getBinString() {
			return toBinaryString(ordinal(), 3);
		}
	}

	enum Destination {
		NULL(0),
		M(1),
		D(2),
		DM(3),
		MD(3),
		A(4),
		AM(5),
		MA(5),
		AD(6),
		DA(6),
		ADM(7),
		AMD(7),
		DMA(7),
		DAM(7),
		MAD(7),
		MDA(7); // commutativity what's that

		private final int num;

		Destination(int num) {
			this.num = num;
		}

		String getBinString() {
			return toBinaryString(num, 3);
		}
	}

	enum Computation {
		ZERO("101010", "0"), ONE("111111", "1"), NEG_ONE("111010", "-1"), D("001100", "D"), A("110000", "A"), NOT_D("001101", "!D"), NOT_A("110001", "!A"), NEG_D("001111", "-D"), D_INC("011111", "D+1"), A_INC("110111", "A+1"), D_DEC("001110", "D-1"), A_DEC("110010", "A-1"), D_ADD_A("000010", "D+A"), A_ADD_D(D_ADD_A.code, "A+D"), D_SUB_A("010011", "D-A"), A_SUB_D("000111", "A-D"), D_AND_A("000000", "D&A"), A_AND_D(D_AND_A.code, "D&A"), D_OR_A("010101", "D|A"), A_OR_D(D_OR_A.code, "A|D"),
		;

		public final String text;
		private final String code;

		Computation(String code, String text) {
			this.text = text;
			this.code = code;
		}

		String getBinString() {
			return code;
		}
	}

	private static class LineException extends ParseException {
		/**
		 * Constructs a ParseException with the specified detail message and
		 * offset.
		 * A detail message is a String that describes this particular exception.
		 *
		 * @param s           the detail message
		 * @param errorOffset the position where the error is found while parsing.
		 */
		public LineException(String s, int line, int errorOffset) {
			super("Line " + line + ": " + s, errorOffset);
		}

		public LineException(String s, int line) {
			this(s, line, 0);
		}

		public LineException(Exception e, int line) {
			super(e.getMessage(), line);
		}
	}
	private static final int
			A_INS_BIT_COUNT = 15;	private static final String
			FILE_NAME = "Rect",
			FILE_PATH = FILE_NAME + ".asm", OUTPUT_PATH = FILE_NAME + ".hack";

	public static void main(String[] args) throws Exception {
		System.out.println("Hello assembler!");

		String path;
		if (args.length == 0) {
			System.out.println("No filepath arg given.");
			path = FILE_PATH;
		} else if (args.length == 1)
			path = args[0];
		else {
			System.err.println("Args must be <=1.");
			return;
		}
		System.out.println("Using filepath: " + path);

		String text = Files.readString(Path.of(path));
		String bin = hackASMtoBinaryString(semiCleanText(text));
		Files.writeString(Path.of(OUTPUT_PATH), bin);
	}

	public static String hackASMtoBinaryString(String text) throws ParseException {
		// stats
		int lineCount = 0, varCount = 0, aiCount = 0, ciCount = 0;

		/* Assign addresses */

		HashMap<String, Integer> addressMap = new HashMap<>();
		// registers
		for (int ri = 0; ri < 16; ri++) {
			addressMap.put("r" + ri, ri);
			addressMap.put("R" + ri, ri);
		}
		addressMap.put("SCREEN", 16384);
		addressMap.put("KBD", 24576);
		// labels
		String[] instructions = text.split("\n");
		for (int i = 0, lineCounter = 0; i < instructions.length; i++) {
			String line = instructions[i];
			if (line.startsWith("(")) {
				if (!line.endsWith(")")) throw new LineException("Label line missing ')' at end", i);
				if (line.indexOf(")") != line.length() - 1)
					throw new LineException("Label closure ')' must appear at line end end with", i, line.indexOf(')'));
				String labelName = line.substring(1, line.length() - 1);
				addressMap.putIfAbsent(labelName, lineCounter);
			}
			if (!(line.isEmpty() || line.startsWith("//") || line.startsWith("(") || line.startsWith("\n")))
				lineCount = ++lineCounter;
		}
		// vars & label calls
		for (int i = 0, varI = 16; i < instructions.length; i++) {
			String line = instructions[i];
			if (line.isEmpty()) continue;
			if (line.charAt(0) == '@') {// 'A' ins
				String labelName = line.substring(1);
				try {
					Integer.parseInt(labelName); // check if @ is literal number already
				} catch (NumberFormatException ignored) {
					Integer labelI = addressMap.get(labelName);
					if (labelI == null) {
						System.out.println("No label registered with name \"" + labelName + "\", assuming var & assigning to @" + varI);
						addressMap.put(labelName, varI);
						varI++;
						varCount++;
					}
				}
			}
		}
		for (String address : addressMap.keySet()) {
			// replace named addresses with number
			// FIXME: For large enough files, at a certain point the regex
			//  seems to fail entirely, and no replacing occurs.
			//  I probably need to learn more about regex & it's implementation here.
			text = text.replaceAll("@" + address + "\n", "@" + addressMap.get(address) + "\n");
			// remove label lines
			text = text.replaceAll("\\(" + address + "\\)\n", "");
		}

		/* Parse */

		StringBuilder asm = new StringBuilder();
		instructions = text.split("\n");
		for (int i = 0; i < instructions.length; i++) {
			String instruction = instructions[i];
			int ci = instruction.indexOf("//");
			if (ci != -1) instruction = instruction.substring(0, ci); // remove line comments

			if (instruction.charAt(0) == '@') {// 'A' ins
				asm.append(0);
				try {
					String a = Integer.toBinaryString(Integer.parseInt(instruction.substring(1)));
					if (a.length() > A_INS_BIT_COUNT) a = a.substring(0, A_INS_BIT_COUNT + 1);
					else if (a.length() < A_INS_BIT_COUNT) asm.append("0".repeat(A_INS_BIT_COUNT - a.length()));
					asm.append(a);
				} catch (NumberFormatException e) {
					throw new LineException(e, i);
				}

				aiCount++;
			} else { // 'C' ins
				// split text to op parts
				String[] ops = new String[3];
				boolean hasDest = instruction.contains("="), hasJump = instruction.contains(";");
				ops[0] = hasDest ? instruction.substring(0, instruction.indexOf("=")) : Destination.NULL.name();
				ops[1] = (hasDest ? instruction.substring(instruction.indexOf("=") + 1) : instruction).split(";")[0];
				ops[2] = hasJump ? instruction.substring(instruction.indexOf(";") + 1) : Jump.NULL.name();

				// dest
				String destText = ops[0];
				Destination destCode = null;
				for (Destination d : Destination.values())
					if (d.name().equalsIgnoreCase(destText)) {
						destCode = d;
						break;
					}
				if (destCode == null) throw new LineException("Could not parse dest code \"" + destText + "\"", i);
				// comp
				String compText = ops[1];
				boolean a = !(compText.contains("m") || compText.contains("M"));
				if (!a) compText = compText.toUpperCase().replaceAll("M", "A");
				Computation compCode = null;
				for (Computation c : Computation.values())
					if (c.text.equalsIgnoreCase(compText)) {
						compCode = c;
						break;
					}
				if (compCode == null) throw new LineException("Could not parse comp code \"" + compText + "\"", i);
				// jmp
				String jumpText = ops[2];
				Jump jumpCode = null;
				for (Jump j : Jump.values())
					if (j.name().equalsIgnoreCase(jumpText)) {
						jumpCode = j;
						break;
					}
				if (jumpCode == null) throw new LineException("Could not parse jump code \"" + jumpText + "\"", i);

				// assemble
				asm.append(111);
				asm.append(a ? 0 : 1);
				asm.append(compCode.getBinString());
				asm.append(destCode.getBinString());
				asm.append(jumpCode.getBinString());

				ciCount++;
			}
			asm.append("\n");
		}

		System.out.println("Parsing complete.");
		System.out.println("Parsed " + lineCount + " clean lines: " + aiCount + " A ins, " + ciCount + " C ins. " + varCount + " variables.");

		return asm.toString();
	}

	private static String semiCleanText(String text) {
		return text.replaceAll(" ", "").replaceAll("(?m)^//.*", "").replaceAll("[\r\n]+", "\n").trim();
	}

	private static String toBinaryString(int n, int minBitCount) {
		String str = Integer.toBinaryString(n);
		if (str.length() < minBitCount) str = "0".repeat(minBitCount - str.length()) + str;
		return str;
	}


}