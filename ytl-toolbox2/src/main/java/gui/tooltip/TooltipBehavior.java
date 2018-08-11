package gui.tooltip;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Tooltipを対象にするPopupBehavior
 *
 * @author nodamushi
 */
public class TooltipBehavior extends TooltipBehaviorBase<Tooltip> {

	public static TooltipBehavior createManagedInstance() {
		final TooltipBehavior t = new TooltipBehavior();
		manageVisible(t);
		return t;
	}

	public static TooltipBehavior createManagedInstance(
			final Duration open, final Duration hide, final Duration left) {
		final TooltipBehavior t = new TooltipBehavior();
		if (open != null) {
			t.setOpenDuration(open);
		}
		if (hide != null) {
			t.setHideDuration(hide);
		}
		if (left != null) {
			t.setLeftDuration(left);
		}
		manageVisible(t);
		return t;
	}

	@Override
	protected void setActivate(final Tooltip p, final Node hover) {
		final Tooltip old = this.getActivatePopup();
		if (old != p) {
			if (old != null) {
				setActivated(old, Boolean.FALSE);
			}
			if (p != null) {
				setActivated(old, Boolean.TRUE);
			}
		}
		super.setActivate(p, hover);
	}

	@Override
	protected void runOpenTimer(final Tooltip p, final Node hover) {
		final Duration d = this.getOpenDuration();
		this.runOpenTimer(d == null ? DEFAULT_OPEN_DURATION : d);
	}

	@Override
	protected void runHideTimer(final Tooltip p, final Node hover) {
		final Duration d = this.getHideDuration();
		this.runHideTimer(d == null ? DEFAULT_HIDE_DURATION : d);
	}

	@Override
	protected void runLeftTimer(final Tooltip p, final Node hover) {
		final Duration d = this.getLeftDuration();
		this.runLeftTimer(d == null ? DEFAULT_LEFT_DURATION : d);
	}

	//-------------------------------------------
	//    一つのTooltipを使い回す
	//-------------------------------------------

	/**
	 * {@link TooltipBehavior#install(Node)}で利用するツールチップを設定する
	 *
	 * @param t Tooltip
	 * @see TooltipBehavior#install(Node)
	 */
	public void setDefaultTooltip(final Tooltip t) {
		this.defaultTooltip = t;
	}

	/**
	 * {@link TooltipBehavior#install(Node)}で利用するツールチップ返す。<br/>
	 * 何も指定されていないときは、新たなTooltipインスタンスを生成、保持してから変えす。
	 *
	 * @return Tooltip
	 */
	private Tooltip getDefaultTooltip() {
		if (this.defaultTooltip == null) {
			this.defaultTooltip = new Tooltip();
		}
		return this.defaultTooltip;
	}

	private Tooltip defaultTooltip;

	/**
	 * {@link TooltipBehavior#getDefaultTooltip()}で得られる
	 * Tooltipを用いてインストールする
	 *
	 * @param node Node
	 */
	private void install(final Node node) {
		final Tooltip t = this.getDefaultTooltip();
		this.install(node, t);
	}

}
