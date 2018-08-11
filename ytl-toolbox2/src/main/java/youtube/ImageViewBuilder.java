package youtube;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewBuilder<T extends ImageView> {
	private final T node;

	public ImageViewBuilder(T node) {
		this.node = node;
	}

	public ImageViewBuilder<T> y(double arg0) {
		this.node.setY(arg0);
		return this;
	}

	public ImageViewBuilder<T> x(double arg0) {
		this.node.setX(arg0);
		return this;
	}

	public ImageViewBuilder<T> fitWidth(double arg0) {
		this.node.setFitWidth(arg0);
		return this;
	}

	public ImageViewBuilder<T> preserveRatio(boolean arg0) {
		this.node.setPreserveRatio(arg0);
		return this;
	}

	public ImageViewBuilder<T> image(Image arg0) {
		this.node.setImage(arg0);
		return this;
	}

	public ImageViewBuilder<T> smooth(boolean arg0) {
		this.node.setSmooth(arg0);
		return this;
	}

	public ImageViewBuilder<T> fitHeight(double arg0) {
		this.node.setFitHeight(arg0);
		return this;
	}

	public ImageViewBuilder<T> viewport(Rectangle2D arg0) {
		this.node.setViewport(arg0);
		return this;
	}

}
