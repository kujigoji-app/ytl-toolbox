package ytltoolbox;

import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

class MyUtil {

	private MyUtil() {
	}

	public static void copy(Object source, Object dest)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
		PropertyDescriptor[] pdList = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : pdList) {
			Method writeMethod = null;
			Method readMethod = null;
			try {
				writeMethod = pd.getWriteMethod();
				readMethod = pd.getReadMethod();
			} catch (Exception ignored) {
			}

			if (readMethod == null || writeMethod == null) {
				continue;
			}

			Object val = readMethod.invoke(source);
			if (val != null) {
				writeMethod.invoke(dest, val);
			}
		}
	}

	/**
	 * @param url
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void openBrowser(String url) {
		try {
			Desktop desktop = Desktop.getDesktop();

			URI uri = new URI(url);
			desktop.browse(uri);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

}
