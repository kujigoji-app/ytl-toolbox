package gui.tooltip;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Duration;

import static java.lang.Math.floor;

/**
 * 変更可能なDurationのプロパティとオフセットのプロパティを持つTooltipBehaviorの基本実装
 *
 * @param <P>
 * @author nodamushi
 */
public abstract class TooltipBehaviorBase<P extends PopupWindow>
		extends SinglePopupBehavior<P> {

	@Override
	protected void show(final P p, final Node hover, double x, double y) {
		//あんまり意味は分かってないけど
		//とりあえずjavafx.scene.control.Tooltipからほぼ引用

		final NodeOrientation nodeOrientation = hover.getEffectiveNodeOrientation();
		p.getScene().setNodeOrientation(nodeOrientation);
		if (nodeOrientation == NodeOrientation.RIGHT_TO_LEFT) {
			x -= p.getWidth();
		}
		final Window owner = getWindow(hover);
		final double ox = this.getXOffset();
		final double oy = this.getYOffset();
		p.show(owner, floor(x + ox), floor(y + oy));

		if ((y + TOOLTIP_YOFFSET) > p.getAnchorY()) {
			p.hide();
			y -= p.getHeight();
			p.show(owner, floor(x + ox), floor(y));
		}
	}

	//-------------------------------------------
	//    Properties
	//-------------------------------------------

	/**
	 * ポップアップさせるまでの時間
	 *
	 * @return
	 */
	final ObjectProperty<Duration> openDurationProperty() {
		if (this.openDurationProperty == null) {
			this.openDurationProperty = new SimpleObjectProperty<>(this, "openDuration", DEFAULT_OPEN_DURATION);
		}
		return this.openDurationProperty;
	}

	final Duration getOpenDuration() {
		return this.openDurationProperty == null ? DEFAULT_OPEN_DURATION : this.openDurationProperty.get();
	}

	public final void setOpenDuration(final Duration value) {
		this.openDurationProperty().set(value);
	}

	private ObjectProperty<Duration> openDurationProperty;

	/**
	 * ポップアップ後、ウィンドウが表示になっている時間
	 *
	 * @return
	 */
	final ObjectProperty<Duration> hideDurationProperty() {
		if (this.hideDurationProperty == null) {
			this.hideDurationProperty = new SimpleObjectProperty<>(this, "hideDuration", DEFAULT_HIDE_DURATION);
		}
		return this.hideDurationProperty;
	}

	final Duration getHideDuration() {
		return this.hideDurationProperty == null ? DEFAULT_HIDE_DURATION : this.hideDurationProperty.get();
	}

	public final void setHideDuration(final Duration value) {
		this.hideDurationProperty().set(value);
	}

	private ObjectProperty<Duration> hideDurationProperty;

	/**
	 * ポップアップが表示テイルときに、ポップアップが表示の原因となったNodeからマウスが放れた後に、
	 * ウィンドウが表示になっている時間
	 *
	 * @return
	 */
	final ObjectProperty<Duration> leftDurationProperty() {
		if (this.leftDurationProperty == null) {
			this.leftDurationProperty = new SimpleObjectProperty<>(this, "leftDuration", DEFAULT_LEFT_DURATION);
		}
		return this.leftDurationProperty;
	}

	final Duration getLeftDuration() {
		return this.leftDurationProperty == null ? DEFAULT_LEFT_DURATION : this.leftDurationProperty.get();
	}

	public final void setLeftDuration(final Duration value) {
		this.leftDurationProperty().set(value);
	}

	private ObjectProperty<Duration> leftDurationProperty;

	/**
	 * 表示時のオフセット
	 *
	 * @return
	 */
	final DoubleProperty xOffsetProperty() {
		if (this.xOffsetProperty == null) {
			this.xOffsetProperty = new SimpleDoubleProperty(this, "xOffset", TOOLTIP_XOFFSET);
		}
		return this.xOffsetProperty;
	}

	final double getXOffset() {
		return this.xOffsetProperty == null ? TOOLTIP_XOFFSET : this.xOffsetProperty.get();
	}

	public final void setXOffset(final double value) {
		this.xOffsetProperty().set(value);
	}

	private DoubleProperty xOffsetProperty;

	/**
	 * 表示時のオフセット
	 *
	 * @return
	 */
	final DoubleProperty yOffsetProperty() {
		if (this.yOffsetProperty == null) {
			this.yOffsetProperty = new SimpleDoubleProperty(this, "yOffset", TOOLTIP_YOFFSET);
		}
		return this.yOffsetProperty;
	}

	final double getYOffset() {
		return this.yOffsetProperty == null ? TOOLTIP_YOFFSET : this.yOffsetProperty.get();
	}

	public final void setYOffset(final double value) {
		this.yOffsetProperty().set(value);
	}

	private DoubleProperty yOffsetProperty;

	static final Duration DEFAULT_OPEN_DURATION = new Duration(1000);
	static final Duration DEFAULT_HIDE_DURATION = new Duration(5000);
	static final Duration DEFAULT_LEFT_DURATION = new Duration(200);
	private static final double TOOLTIP_XOFFSET = 10;
	private static final double TOOLTIP_YOFFSET = 7;
}
