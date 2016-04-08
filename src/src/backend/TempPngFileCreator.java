package backend;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TempPngFileCreator {

	public static File createTmpPngCopy(String canonicalPath) throws IOException {
		final String prefix = "opencv", suffix = ".png";
		File tmpPngFile = File.createTempFile(prefix, suffix);
		File input = new File(canonicalPath);
		ImageIO.write(ImageIO.read(input), "png", tmpPngFile);
		return tmpPngFile;
	}
}
