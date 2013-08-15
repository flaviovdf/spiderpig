package br.ufmg.dcc.vod.spiderpig.filesaver;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.junit.Test;

import com.google.common.io.Files;

public class LevelDBSaverTest {

	@Test
	public void testSave() throws IOException, InterruptedException {
		File tmpFolder = Files.createTempDir();
		File tmpFile = new File(tmpFolder, "db.db");
		
		LevelDBSaver impl = new LevelDBSaver(tmpFile.getAbsolutePath(), false);

		impl.save("bah1", "oi\nquer\ntc".getBytes());
		impl.save("bah2", "oi2\nquer\ntc".getBytes());
		impl.close();
		
		Options options = new Options();
		DBFactory factory = new JniDBFactory();
		DB db = factory.open(new File(tmpFile.getAbsolutePath()), options);
		
		byte[] bah1 = db.get("bah1".getBytes());
		byte[] bah2 = db.get("bah2".getBytes());
		
		Assert.assertEquals(new String(bah1), "oi\nquer\ntc");
		Assert.assertEquals(new String(bah2), "oi2\nquer\ntc");
	}

}
