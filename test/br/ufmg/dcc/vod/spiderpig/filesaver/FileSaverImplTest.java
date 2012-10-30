package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import br.ufmg.dcc.vod.spiderpig.common.FileUtil;
import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaverImpl;

import com.google.common.io.Files;

public class FileSaverImplTest {

	@Test
	public void testSave() throws IOException {
		File tmp = Files.createTempDir();
		FileSaverImpl impl = new FileSaverImpl(tmp.getAbsolutePath());

		impl.save("bah", "oi\nquer\ntc".getBytes());
		
		List<String> fileAsList = FileUtil.readFileToList(new File(tmp, "bah"));

		Assert.assertEquals(3, fileAsList.size());
		Assert.assertEquals(Arrays.asList("oi", "quer", "tc"), fileAsList);
	}

}
