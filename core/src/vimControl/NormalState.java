/**
 *
 */
package vimControl;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

/**
 * @author 楊舜宇
 * @since 2016/5/29
 *
 */
public enum NormalState implements State<NormalMode>{
	NONE() {
		@Override
		public void update(NormalMode normal) {
			switch (normal.getKey()) {
			case GameKeys.j:
				normal.normal.changeState(MOVE_DOWN);
				break;
			case GameKeys.k:
				normal.normal.changeState(MOVE_UP);
				break;
			case GameKeys.h:
				normal.normal.changeState(MOVE_LEFT);
				break;
			case GameKeys.l:
				normal.normal.changeState(MOVE_RIGHT);
				break;
			default:
				break;
			}
		}
	},

	MOVE_DOWN() {
		@Override
		public void enter(NormalMode normal) {
			normal.normal.changeState(NONE);
		}
	},

	MOVE_UP() {
		@Override
		public void enter(NormalMode normal) {
			normal.normal.changeState(NONE);
		}
	},

	MOVE_LEFT() {
		@Override
		public void enter(NormalMode normal) {
			normal.normal.changeState(NONE);
		}
	},

	MOVE_RIGHT() {
		@Override
		public void enter(NormalMode normal) {
			normal.normal.changeState(NONE);
		}
	};

	@Override
	public void enter(NormalMode entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(NormalMode entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(NormalMode entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMessage(NormalMode entity, Telegram telegram) {
		// TODO Auto-generated method stub
		return false;
	}

}
