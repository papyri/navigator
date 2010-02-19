package java.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;

public interface SQLXML {
    public void free() throws SQLException;
    public InputStream getBinaryStream() throws SQLException;
    public OutputStream setBinaryStream() throws SQLException;
    public Reader getCharacterStream() throws SQLException;
    public Writer setCharacterStream() throws SQLException;
    public String getString() throws SQLException;
    public void setString(String str) throws SQLException;
    public Source getSource(Class clazz) throws SQLException;
    public Result setResult(Class clazz) throws SQLException;
}

