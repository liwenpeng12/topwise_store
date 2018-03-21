package com.topwise.topos.appstore.conn.jsonable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author ganggang
 *
 */
public interface Streamable {
	public void writeToStream(OutputStream dest) throws IOException;
	public void readFromStream(InputStream source) throws IOException;
//  public interface Creator<T extends Streamable> {
//      public T createFromStream(InputStream source) throws IOException;
//  }
}
