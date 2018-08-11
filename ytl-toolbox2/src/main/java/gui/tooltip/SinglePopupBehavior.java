package gui.tooltip;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Duration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Tooltipなどのように、常に最大で一つのポップアップしか表示されないような
 * 挙動を定義する。<br/>
 * 基本的な動作は javafx.scene.control.Tooltip.TooltipBehavior
 * と同様になるようにしてある。<br/><br/>
 * 複数のSinglePopupBehaviorの間でも最大で一つのポップアップしか表示させたくない
 * 場合は、BehaviorGroupを作成し、登録する。
 *
 * @param <P>
 * @author nodamushi
 */
public abstract class SinglePopupBehavior<P extends PopupWindow> {

	/**
	 * 初期化をする。最初にinstallが実行されたときに呼び出される
	 */
	void initialize() {
		if (!this.initialized) {
			this.initialized = true;
			this.open = new Timeline();
			this.hide = new Timeline();
			this.left = new Timeline();

			this.open.setOnFinished(this::openAction);
			this.hide.setOnFinished(this::hideAction);
			this.left.setOnFinished(this::leftAction);

			this.move = this::mouseMove;
			this.exit = this::mouseExited;
			this.press = this::mousePressed;
		}
	}

	/**
	 * Nodeにマウスがホバーしたとき、ポップアップするようインストールする。<br/>
	 * nに複数回インストールした際の動作は保証しない。
	 *
	 * @param n
	 * @param p
	 */
	public void install(final Node n, final P p) {
		if (n == null || p == null) {
			return;
		}
		if (!this.initialized) {
			this.initialize();
		}
		n.addEventHandler(MouseEvent.MOUSE_MOVED, this.move);
		n.addEventHandler(MouseEvent.MOUSE_EXITED, this.exit);
		n.addEventHandler(MouseEvent.MOUSE_PRESSED, this.press);
		this.storePopup(n, p);
	}

	/**
	 * インストールした内容を削除する
	 *
	 * @param n
	 */
	public void uninstall(final Node n) {
		if (n == null || !this.initialized) {
			return;
		}
		n.removeEventHandler(MouseEvent.MOUSE_MOVED, this.move);
		n.removeEventHandler(MouseEvent.MOUSE_EXITED, this.exit);
		n.removeEventHandler(MouseEvent.MOUSE_PRESSED, this.press);
		final P p = this.getPopup(n);
		if (p != null && this.getVisiblePopup() == p && this.getVisibleNode() == n) {
			p.hide();
			this.setVisible(null, null);
			this.stopHideTimer();
			this.stopLeftTimer();
		}
		this.storePopup(n, null);
	}

	//-----------------------------------------------
	//         Hook
	//-----------------------------------------------

	/**
	 * Popupが表示可能かどうかの判断を行うBiPredicateを設定する
	 *
	 * @param p null可
	 */
	public void setDisplayableChecker(final BiPredicate<? super P, ? super Node> p) {
		this.visibityc = p;
	}

	/**
	 * {@link SinglePopupBehavior#setDisplayableChecker(BiPredicate)}で設定された内容を返す
	 *
	 * @return
	 */
	BiPredicate<? super P, ? super Node> getDisplayableChecker() {
		return this.visibityc;
	}

	/**
	 * {@link SinglePopupBehavior#getDisplayableChecker()}がnullでない場合は、
	 * {@link BiPredicate#test(Object, Object)}の結果を返し、nullである場合はtrueを返す
	 *
	 * @param p     表示するポップアップ
	 * @param hover
	 * @return
	 */
	boolean checkDisplayable(final P p, final Node hover) {
		final BiPredicate<? super P, ? super Node> b = this.getDisplayableChecker();
		return b == null || b.test(p, hover);
	}

	/**
	 * pを表示することが可能かどうか判断する
	 *
	 * @param p     表示するポップアップ
	 * @param hover
	 * @return
	 */
	boolean isDisplayable(final P p, final Node hover) {
		final Window w = getWindow(hover);
		return w != null && isWindowHierarchyVisible(hover)
				&& (this.isPopupOnNonFocusWindow(p, hover) || hasFocus(w)) && this.checkDisplayable(p, hover);
	}

	/**
	 * hoverにマウスがあるときに、pを表することが可能かどうかを判断する。<br/>
	 * <p>
	 * {@link SinglePopupBehavior#isDisplayable(PopupWindow, Node)}から利用される。
	 *
	 * @param p     表示するポップアップ
	 * @param hover
	 * @return
	 * @see SinglePopupBehavior#isPopupOnNonFocusWindow()
	 */
	boolean isPopupOnNonFocusWindow(final P p, final Node hover) {
		return this.isPopupOnNonFocusWindow();
	}

	/**
	 * 表示する前に、ポップアップの内容を更新するBiConsumerを登録する
	 *
	 * @param updater
	 */
	public void setPopupUpdater(final BiConsumer<? super P, ? super Node> updater) {
		this.update = updater;
	}

	/**
	 * {@link SinglePopupBehavior#setPopupUpdater(BiConsumer)}で設定された内容を返す
	 *
	 * @return
	 * @see SinglePopupBehavior#setPopupUpdater(BiConsumer)
	 */
	BiConsumer<? super P, ? super Node> getPopupUpdater() {
		return this.update;
	}

	/**
	 * popupUpdaterが登録されていれば、更新を行う
	 *
	 * @param p    表示するポップアップ
	 * @param node マウスがホバーされているノード
	 * @see SinglePopupBehavior#setPopupUpdater(BiConsumer)
	 */
	void updatePopup(final P p, final Node node) {
		final BiConsumer<? super P, ? super Node> u = this.getPopupUpdater();
		if (u != null) {
			u.accept(p, node);
		}
	}

	//-----------------------------------------------
	//         MouseEvent
	//-----------------------------------------------

	/**
	 * このBehaviorの利用するEventHandlerで観測された最後のマウススクリーン座標のx
	 *
	 * @return 観測された最後のマウススクリーン座標のx
	 * @see SinglePopupBehavior#getLastMouseY()
	 */
	final double getLastMouseX() {
		return this.x;
	}

	/**
	 * このBehaviorの利用するEventHandlerで観測された最後のマウススクリーン座標のy
	 *
	 * @return 観測された最後のマウススクリーン座標のy
	 * @see SinglePopupBehavior#getLastMouseX()
	 */
	final double getLastMouseY() {
		return this.y;
	}

	/**
	 * マウスのスクリーン座標を保持する
	 *
	 * @param x スクリーン座標x
	 * @param y スクリーン座標y
	 */
	final void setMousePosition(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * マウスのスクリーン座標を保持する
	 *
	 * @param e MouseEvent
	 */
	final void setMousePosition(final MouseEvent e) {
		this.setMousePosition(e.getScreenX(), e.getScreenY());
	}

	/**
	 * {@link MouseEvent#MOUSE_MOVED}のイベント発生時に呼び出されます。
	 *
	 * @param e
	 */
	private void mouseMove(final MouseEvent e) {
		this.setMousePosition(e);
		final Object source = e.getSource();
		if (!(source instanceof Node)) {
			return;
		}
		final Node hover = (Node) source;
		final P p = this.getPopup(hover);
		if (p == null) {
			return;
		}
		final P v = this.getVisiblePopup();
		final boolean othershow = this.isOtherBehaviorsShowing();
		if (othershow || v != null) {
			if ((othershow || (p != v || hover != this.getVisibleNode()))
					&& this.isDisplayable(p, hover)) {
				if (v != null) {
					v.hide();
				}
				this.stopLeftTimer();
				this.stopOpenTimer();
				final double x = this.getLastMouseX();
				final double y = this.getLastMouseY();
				this.updatePopup(p, hover);
				this.show(p, hover, x, y);
				this.killOtherBehaviors();
				this.setVisible(p, hover);
				this.runHideTimer(p, hover);
			}
		} else {
			this.stopLeftTimer();
			this.stopHideTimer();
			this.setActivate(p, hover);
			this.runOpenTimer(p, hover);
		}

	}

	/**
	 * {@link MouseEvent#MOUSE_EXITED}のイベント発生時に呼び出されます。
	 *
	 * @param e
	 */
	private void mouseExited(final MouseEvent e) {
		this.setMousePosition(e);
		final P v = this.getVisiblePopup();
		if (v != null) {
			if (this.isHideOnExit(v, this.getVisibleNode())) {
				this.kill();
			} else {
				this.stopOpenTimer();
				this.stopHideTimer();
				this.runLeftTimer(v, this.getVisibleNode());
			}
		} else {
			this.stopOpenTimer();
			this.stopLeftTimer();
			this.stopHideTimer();
			this.setActivate(null, null);
		}
	}

	/**
	 * {@link MouseEvent#MOUSE_EXITED}が発生したとき、Left Timerを起動するのではなく、
	 * 即座に非表示にするかどうかを判断する。
	 *
	 * @param p    非表示にするポップアップ
	 * @param node MOUSE_EXITEDの発生元
	 * @return trueの時、即座に非表示にする
	 * @see SinglePopupBehavior#isHideOnExit()
	 */
	boolean isHideOnExit(final P p, final Node node) {
		return this.isHideOnExit();
	}

	/**
	 * {@link MouseEvent#MOUSE_PRESSED}のイベント発生時に呼び出されます。
	 *
	 * @param e
	 */
	private void mousePressed(final MouseEvent e) {
		this.setMousePosition(e);
		this.kill();
	}

	//-----------------------------------------------
	//         Timer
	//-----------------------------------------------

	/**
	 * ポップアップを表示するタイマーのアクション
	 *
	 * @param e
	 */
	private void openAction(final ActionEvent e) {
		final P p = this.getActivatePopup();
		final Node n = this.getHoverNode();
		this.setActivate(null, null);
		if (this.isDisplayable(p, n)) {
			this.stopLeftTimer();
			this.stopOpenTimer();
			final double x = this.getLastMouseX();
			final double y = this.getLastMouseY();
			this.updatePopup(p, n);
			this.show(p, n, x, y);
			this.killOtherBehaviors();
			this.setVisible(p, n);
			this.stopLeftTimer();
			this.runHideTimer(p, n);
		}
	}

	/**
	 * 表示してからの時間経過でポップアップを非表示にするタイマーのアクション
	 *
	 * @param e
	 */
	private void hideAction(final ActionEvent e) {
		this.kill();
	}

	/**
	 * Nodeからマウスが離れてからの時間経過でポップアップを表示するタイマーのアクション
	 *
	 * @param e
	 */
	private void leftAction(final ActionEvent e) {
		this.kill();
	}

	private static boolean isStopped(final Timeline t) {
		return t.getStatus() == Status.STOPPED;
	}

	private static boolean isRunning(final Timeline t) {
		return t.getStatus() == Status.RUNNING;
	}

	/**
	 * ポップアップを表示するタイマーが動いているかどうか
	 *
	 * @return
	 */
	protected final boolean isOpenRunning() {
		return isRunning(this.open);
	}

	/**
	 * 表示してからの時間経過でポップアップを非表示にするタイマーが動いているかどうか
	 *
	 * @return
	 */
	protected final boolean isHideRunning() {
		return isRunning(this.hide);
	}

	/**
	 * Nodeからマウスが離れてからの時間経過でポップアップを表示するタイマーが動いているかどうか
	 *
	 * @return
	 */
	protected final boolean isLeftRunning() {
		return isRunning(this.left);
	}

	/**
	 * ポップアップを表示するタイマーが止まっているかどうか
	 *
	 * @return
	 */
	protected final boolean isOpenStopped() {
		return isStopped(this.open);
	}

	/**
	 * 表示してからの時間経過でポップアップを非表示にするタイマーが止まっているかどうか
	 *
	 * @return
	 */
	protected final boolean isHideStopped() {
		return isStopped(this.hide);
	}

	/**
	 * Nodeからマウスが離れてからの時間経過でポップアップを表示するタイマーが動いているかどうか
	 *
	 * @return
	 */
	protected final boolean isLeftStopped() {
		return isStopped(this.left);
	}

	private static void runTimer(final Duration d, final Timeline t) {
		if (!isStopped(t)) {
			t.stop();
		}
		final ObservableList<KeyFrame> list = t.getKeyFrames();
		if (d != null) {
			if (list.isEmpty()) {
				list.add(new KeyFrame(d));
			} else if (!list.get(0).getTime().equals(d)) {
				list.setAll(new KeyFrame(d));
			}
		} else if (list.isEmpty()) {
			list.add(new KeyFrame(Duration.INDEFINITE));
		}
		t.playFromStart();
	}

	/**
	 * ポップアップを表示するタイマーを起動する<br/>
	 * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runOpenTimer(Duration)}
	 * を呼び出す
	 *
	 * @param p     表示にするポップアップ
	 * @param hover pが表示になった原因のNode
	 */
	protected abstract void runOpenTimer(P p, Node hover);

	/**
	 * ポップアップを表示するタイマーを起動する<br/>
	 * 渡されたDurationの間待機し、Popupを表示するタイマーを起動する
	 *
	 * @param d アクションまでの時間
	 */
	final void runOpenTimer(final Duration d) {
		runTimer(d, this.open);
	}

	/**
	 * ポップアップを表示するタイマーを停止する
	 */
	final void stopOpenTimer() {
		this.open.stop();
	}

	/**
	 * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runHideTimer(Duration)}
	 * を呼び出す
	 *
	 * @param p     非表示にする対象のポップアップ
	 * @param hover pが表示になった原因のNode
	 */
	protected abstract void runHideTimer(P p, Node hover);

	/**
	 * 表示してからの時間経過でポップアップを非表示にするタイマーを起動する<br/>
	 * 渡されたDurationの間待機し、Popupを非表示にするタイマーを起動する。<br/>
	 * runOpenTimerがイベントを発行した後に使われる<br/>
	 *
	 * @param d アクションまでの時間
	 */
	final void runHideTimer(final Duration d) {
		runTimer(d, this.hide);
	}

	/**
	 * 表示してからの時間経過でポップアップを非表示にするタイマーを停止する<br/>
	 */
	final void stopHideTimer() {
		this.hide.stop();
	}

	/**
	 * Nodeからマウスが放れてからの時間経過でポップアップを非表示にするタイマーを起動する<br/>
	 * 対象に見合ったDurationを計算し、{@link SinglePopupBehavior#runLeftTimer(Duration)}
	 * を呼び出す
	 *
	 * @param p     非表示にする対象のポップアップ
	 * @param hover pが表示になった原因のNode
	 */
	protected abstract void runLeftTimer(P p, Node hover);

	/**
	 * Nodeからマウスが放れてからの時間経過でポップアップを非表示にするタイマーを起動する<br/>
	 * 渡されたDurationの間待機し、Popupを非表示にするタイマーを起動する
	 *
	 * @param d アクションまでの時間
	 */
	final void runLeftTimer(final Duration d) {
		runTimer(d, this.left);
	}

	/**
	 * Nodeからマウスが放れてからの時間経過でポップアップを非表示にするタイマーを停止する
	 */
	final void stopLeftTimer() {
		this.left.stop();
	}

	/**
	 * 表示しているポップアップを非表示にし、全てのタイマーを止める
	 */
	void kill() {
		final P v = this.getVisiblePopup();
		if (v != null) {
			v.hide();
		}
		this.setVisible(null, null);
		this.setActivate(null, null);
		this.stopOpenTimer();
		this.stopHideTimer();
		this.stopLeftTimer();
	}

	//-----------------------------------------------
	//         Node
	//-----------------------------------------------

	/**
	 * ポップアップを表示する
	 *
	 * @param p       表示にするポップアップ
	 * @param hover   pが表示になる原因のNode
	 * @param anchorX pのAnchor座標x
	 * @param anchorY pのAnchor座標y
	 */
	protected abstract void show(P p, Node hover, double anchorX, double anchorY);

	/**
	 * nodeからpを取り出せるように保存します。<br/>
	 * 既にnodeに他のpが割り当てられている場合は、上書きします<br/>
	 * また、p=nullの場合はnodeに関する情報を削除します。<br/>
	 * デフォルト実装では{@link Node#getProperties()}を用います。
	 *
	 * @param node
	 * @param p
	 */
	void storePopup(final Node node, final P p) {
		if (p == null) {
			node.getProperties().remove(PROPERTY_KEY);
		} else {
			node.getProperties().put(PROPERTY_KEY, p);
		}
	}

	/**
	 * nodeからPopupを取り出します。<br/>
	 * デフォルト実装は型安全ではありません。
	 *
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	P getPopup(final Node node) {
		final Object o = node.getProperties().get(PROPERTY_KEY);
		if (o instanceof PopupWindow) {
			return (P) o;
		}
		return null;
	}

	/**
	 * ActivatePopupに関連づけられたNode
	 */
	Node getHoverNode() {
		return this.hover;
	}

	/**
	 * VisiblePopupに関連づけられたNode
	 */
	Node getVisibleNode() {
		return this.vinode;
	}

	/**
	 * 表示しようと待機中のPopup
	 */
	P getActivatePopup() {
		return this.activate;
	}

	/**
	 * 現在表示中のPopup
	 */
	P getVisiblePopup() {
		return this.visible;
	}

	/**
	 * 表示待機中のPopupを設定する
	 */
	void setActivate(final P p, final Node hover) {
		this.activate = p;
		this.hover = hover;
	}

	/**
	 * 表示中のPopupを設定する
	 */
	void setVisible(final P p, final Node node) {
		this.visible = p;
		this.vinode = node;
	}

	/**
	 * 表示中のPopupがあるかどうか
	 */
	boolean isShowing() {
		return this.getVisiblePopup() != null;
	}

	//-----------------------------------------------
	//         Group
	//-----------------------------------------------

	/**
	 * 同じグループのBehaviorで現在動いている動作を停止させます
	 */
	final void killOtherBehaviors() {
		if (this.groups == null) {
			return;
		}
		for (final BehaviorGroup g : this.groups) {
			g.killOthers(this);
		}
	}

	/**
	 * 同じグループのBehaviorで現在ポップアップを表示中の要素があるかどうか
	 *
	 * @return
	 */
	final boolean isOtherBehaviorsShowing() {
		if (this.groups == null) {
			return false;
		}
		for (final BehaviorGroup g : this.groups) {
			if (g.isShowing(this)) {
				return true;
			}
		}
		return false;
	}

	private void addGroup(final BehaviorGroup g) {
		if (this.groups == null) {
			this.groups = new ArrayList<>(1);
		}
		this.groups.add(g);
	}

	private void removeGroup(final BehaviorGroup g) {
		if (this.groups == null) {
			return;
		}
		this.groups.remove(g);
		if (this.groups.isEmpty()) {
			this.groups = null;
		}
	}

	/**
	 * 参加しているグループを返す
	 *
	 * @return 不変リスト
	 */
	public List<BehaviorGroup> getGroups() {
		if (this.groups == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(this.groups);
	}

	/**
	 * PopupBehaviorの集合。<br/>
	 * このグループ内では一つだけポップアップを表示するように
	 * PopupBehaviorは動作する<br/>
	 *
	 * @author nodamushi
	 */
	public static class BehaviorGroup {
		private List<WeakReference<SinglePopupBehavior<?>>> behaviors;

		BehaviorGroup() {
		}

		public BehaviorGroup(final SinglePopupBehavior<?>... behaviors) {
			this.addAll(behaviors);
		}

		/**
		 * ブルー婦に属するBehaviorの処理を停止させます
		 */
		public void kill() {
			if (this.behaviors == null) {
				return;
			}
			for (final WeakReference<SinglePopupBehavior<?>> r : this.behaviors) {
				final SinglePopupBehavior<?> b = r.get();
				if (b != null) {
					b.kill();
				}
			}
		}

		private void killOthers(final SinglePopupBehavior<?> source) {
			if (this.behaviors == null) {
				return;
			}
			for (final WeakReference<SinglePopupBehavior<?>> r : this.behaviors) {
				final SinglePopupBehavior<?> b = r.get();
				if (b != null && b != source) {
					b.kill();
				}
			}
		}

		private boolean isShowing(final SinglePopupBehavior<?> source) {
			if (this.behaviors == null) {
				return false;
			}
			for (final WeakReference<SinglePopupBehavior<?>> r : this.behaviors) {
				final SinglePopupBehavior<?> b = r.get();
				if (b != null && b != source && b.isShowing()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * このグループがbehaviorを含むかどうか
		 *
		 * @param behaivor
		 * @return
		 */
		boolean contains(final SinglePopupBehavior<?> behaivor) {
			if (behaivor == null || this.behaviors == null) {
				return false;
			}
			for (final WeakReference<SinglePopupBehavior<?>> r : this.behaviors) {
				final SinglePopupBehavior<?> b = r.get();
				if (behaivor.equals(b)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * このグループ内で表示中のPopupBehaivorが存在するかどうか
		 */
		public boolean isShowing() {
			if (this.behaviors == null) {
				return false;
			}
			for (final WeakReference<SinglePopupBehavior<?>> r : this.behaviors) {
				final SinglePopupBehavior<?> b = r.get();
				if (b != null && b.isShowing()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * グループにbehaviorを追加する
		 *
		 * @param behavior
		 */
		void add(final SinglePopupBehavior<?> behavior) {
			if (behavior == null || this.contains(behavior)) {
				return;
			}
			if (this.behaviors == null) {
				this.behaviors = new ArrayList<>();
			} else if (!this.behaviors.isEmpty()) {
				this.clean();
			}
			final WeakReference<SinglePopupBehavior<?>> w = new WeakReference<>(behavior);
			this.behaviors.add(w);
			behavior.addGroup(this);
		}

		/**
		 * グループからbehaviorを削除する
		 *
		 * @param behavior
		 */
		void remove(final SinglePopupBehavior<?> behavior) {
			if (behavior == null || this.behaviors == null) {
				return;
			}
			final Iterator<WeakReference<SinglePopupBehavior<?>>> i = this.behaviors.iterator();
			while (i.hasNext()) {
				final WeakReference<SinglePopupBehavior<?>> w = i.next();
				final SinglePopupBehavior<?> b = w.get();
				if (b == null) {
					i.remove();
				} else if (b.equals(behavior)) {
					i.remove();
					b.removeGroup(this);
					break;
				}
			}
			while (i.hasNext()) {
				if (i.next().get() == null) {
					i.remove();
				}
			}
			if (this.behaviors.isEmpty()) {
				this.behaviors = null;
			}
		}

		/**
		 * グループから全てのbehaviorを削除する
		 *
		 * @param behaviors
		 */
		void removeAll(final Collection<SinglePopupBehavior<?>> behaviors) {
			if (this.behaviors == null || behaviors == null) {
				return;
			}
			for (final SinglePopupBehavior<?> r : behaviors) {
				if (r == null) {
					continue;
				}
				for (final Iterator<WeakReference<SinglePopupBehavior<?>>> i = this.behaviors.iterator(); i
						.hasNext(); ) {
					final WeakReference<SinglePopupBehavior<?>> w = i.next();
					final SinglePopupBehavior<?> b = w.get();
					if (b != null && b.equals(r)) {
						i.remove();
						b.removeGroup(this);
						break;
					}
				}
			}
			this.clean();
			if (this.behaviors.isEmpty()) {
				this.behaviors = null;
			}
		}

		/**
		 * グループから全てのbehaviorを削除する
		 *
		 * @param behaviors
		 */
		void removeAll(final SinglePopupBehavior<?>... behaviors) {
			if (behaviors == null) {
				return;
			}
			this.removeAll(Arrays.asList(behaviors));
		}

		/**
		 * グループに全てのbehaviorを追加する
		 *
		 * @param behaviors
		 */
		void addAll(final SinglePopupBehavior<?>... behaviors) {
			if (behaviors == null) {
				return;
			}
			this.addAll(Arrays.asList(behaviors));
		}

		/**
		 * グループに全てのbehaviorを追加する
		 *
		 * @param behaviors
		 */
		void addAll(final Collection<SinglePopupBehavior<?>> behaviors) {
			if (behaviors == null) {
				return;
			}
			if (this.behaviors == null) {
				this.behaviors = new ArrayList<>(behaviors.size());
			}
			for (final SinglePopupBehavior<?> b : behaviors) {
				this.add(b);
			}
		}

		private void clean() {
			this.behaviors.removeIf(w1 -> w1.get() == null);
		}

		public void clear() {
			for (final WeakReference<SinglePopupBehavior<?>> w : this.behaviors) {
				final SinglePopupBehavior<?> b = w.get();
				if (b != null) {
					b.removeGroup(this);
					break;
				}
			}
			this.behaviors.clear();
		}

	}

	private static final BehaviorGroup GROUP = new BehaviorGroup();

	/**
	 * SinglePopupBehaviorが管理するBehaviorGroupにBehaviorを追加する
	 *
	 * @param behavior
	 */
	static void manageVisible(final SinglePopupBehavior<?> behavior) {
		GROUP.add(behavior);
	}

	/**
	 * SinglePopupBehaviorが管理するBehaviorGroupにBehaviorを追加する
	 *
	 * @param behaviors
	 */
	public static void manageVisible(final SinglePopupBehavior<?>... behaviors) {
		GROUP.addAll(behaviors);
	}

	/**
	 * SinglePopupBehaviorが管理するBehaviorGroupからBehaviorを削除する
	 *
	 * @param behavior
	 */
	public static void unmanageVisible(final SinglePopupBehavior<?> behavior) {
		GROUP.remove(behavior);
	}

	/**
	 * SinglePopupBehaviorが管理するBehaviorGroupからBehaviorを削除する
	 *
	 * @param behaviors
	 */
	public static void unmanageVisible(final SinglePopupBehavior<?>... behaviors) {
		GROUP.removeAll(behaviors);
	}

	/**
	 * SinglePopupBehaviorが管理するBehaviorGroupを返します
	 *
	 * @return
	 */
	public static BehaviorGroup getStaticGroup() {
		return GROUP;
	}

	//-----------------------------------------------
	//         Utility for sub classes
	//-----------------------------------------------

	private static Method SET_ACTIVATED;
	private static boolean SET_ACTICATED_LOADED = false;

	private static void loadSetActivated() {
		Method m;
		try {
			m = Tooltip.class.getDeclaredMethod("setActivated", boolean.class);
			m.setAccessible(true);
		} catch (final Exception e) {
			m = null;
		}
		SET_ACTIVATED = m;
		SET_ACTICATED_LOADED = true;
	}

	/**
	 * Reflectionを用いてTooltip.setActivatedを呼び出す
	 */
	static void setActivated(final Tooltip t, final Boolean b) {
		if (SET_ACTIVATED == null) {
			if (SET_ACTICATED_LOADED) {
				return;
			} else {
				loadSetActivated();
				if (SET_ACTIVATED == null) {
					return;
				}
			}
		}
		try {
			SET_ACTIVATED.invoke(t, b);
		} catch (final Exception ignored) {
		}
	}

	/**
	 * 親ウィンドウを取得する
	 *
	 * @param n Node.enable {@code null}
	 */
	static Window getWindow(final Node n) {
		if (n == null) {
			return null;
		}
		final Scene s = n.getScene();
		return s == null ? null : s.getWindow();
	}

	/**
	 * ウィンドウがフォーカスを持っているかどうか
	 *
	 * @param w Window.enable {@code null}
	 */
	private static boolean hasFocus(final Window w) {
		return w != null && w.isFocused();
	}

	/**
	 * Nodeがウィンドウ内で可視かどうか。<br/>
	 * javafx.scene.control.Tooltipからほぼ丸々コピペ
	 */
	private static boolean isWindowHierarchyVisible(final Node node) {
		if (node == null || !node.isVisible()) {
			return false;
		}
		boolean treeVisible = true;
		Parent parent = node.getParent();
		while (parent != null && treeVisible) {
			treeVisible = parent.isVisible();
			parent = parent.getParent();
		}
		return treeVisible;
	}

	//-----------------------------------------------
	//         Field
	//-----------------------------------------------
	private boolean initialized = false;
	private Timeline open, hide, left;
	private P activate, visible;
	private Node hover, vinode;
	private List<BehaviorGroup> groups;
	private double x, y;
	private EventHandler<MouseEvent> move, exit, press;
	private BiPredicate<? super P, ? super Node> visibityc;
	private BiConsumer<? super P, ? super Node> update;
	private static final String PROPERTY_KEY = "nodamushi.jfx.popup.PopupBehavior.PROPERTY_KEY";

	/**
	 * フォーカスがないウィンドウでもポップアップするかどうか<br/>
	 * デフォルトはfalse
	 *
	 * @return
	 */
	final BooleanProperty popupOnNonFocusWindowProperty() {
		if (this.popupOnNonFocusWindowProperty == null) {
			this.popupOnNonFocusWindowProperty = new SimpleBooleanProperty(this, "popupOnNonFocusWindow", false);
		}
		return this.popupOnNonFocusWindowProperty;
	}

	final boolean isPopupOnNonFocusWindow() {
		return this.popupOnNonFocusWindowProperty != null && this.popupOnNonFocusWindowProperty.get();
	}

	public final void setPopupOnNonFocusWindow(final boolean value) {
		this.popupOnNonFocusWindowProperty().set(value);
	}

	private BooleanProperty popupOnNonFocusWindowProperty;

	/**
	 * {@link MouseEvent#MOUSE_EXITED}が発生したときにすぐにポップアップを消すかどうか
	 *
	 * @return
	 */
	final BooleanProperty hideOnExitProperty() {
		if (this.hideOnExitProperty == null) {
			this.hideOnExitProperty = new SimpleBooleanProperty(this, "hideOnExit", false);
		}
		return this.hideOnExitProperty;
	}

	final boolean isHideOnExit() {
		return this.hideOnExitProperty != null && this.hideOnExitProperty.get();
	}

	public final void setHideOnExit(final boolean value) {
		this.hideOnExitProperty().set(value);
	}

	private BooleanProperty hideOnExitProperty;

}
