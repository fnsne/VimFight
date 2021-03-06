/**
 *
 */
package game.Object;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import game.Map.GameMap;
import game.Map.Position;
import game.component.Hp;
import game.component.Mp;
import game.component.Score;
import game.component.Status;
import game.gamestates.PlayState;
import vimControl.GameKeys;
import vimControl.VimControl;
import vimControl.VimMode;

/**
 * @author 楊舜宇
 * @since 2016/6/9
 *
 */
public class Player extends Actor implements Creature{
	private static float SQUARE_LENGTH = 30;
	public Position pos;
	private GameMap map;
	private CharacterAnimation animation;
	private boolean isFindCharState = false;
	private boolean isDeleteState = false;
	private int findCharDirection = 1;
	private float accumulateTime = 0f;
	private int repeatTime = 1;
	private boolean isImmortal;
	public PlayState playControl;
	public Hp hp;
	public Mp mp;
	public Score score;
	private int lastPosX = 0;
	private int lastPosY = 0;
	private int movePlusMP;
	public int []statistic = new int[10];
	public Status cmdBar;
	public VimControl vim;
	private float lastStaticTime;
	private boolean isStill = true;
	private Sound stillDemage;

	public Player() {
		hp = new Hp(1000);
		mp = new Mp(1000);
		score = new Score();
		hp.setFull();
		mp.setEmpty();
		cmdBar = new Status();
		vim = new VimControl(cmdBar);
		//setBounds(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
		setTouchable(Touchable.enabled);
		setPosition(55f, 630f);
		pos = new Position(0, 0);
		animation = new CharacterAnimation("images/player1.atlas");
		animation.setSpriteBatch(new SpriteBatch());
		animation.setOrgPos(105f, 630f);
		lastStaticTime = 0f;
		stillDemage = Gdx.audio.newSound(Gdx.files.internal("sound/stillDemage.mp3"));

		addListener(new InputListener(){
			@Override
			public boolean keyTyped(InputEvent event, char keyChar) {
				if((int)keyChar == 0) return false;
				VimMode preMode = vim.getCurrentState();
				vim.inputKey(keyChar);
				switch (vim.getCurrentState()) {
				case NORMAL:
					if(preMode == VimMode.COMMAND && (keyChar == GameKeys.ENTER || keyChar == GameKeys.ESC)) {
						proccessCommandMode(keyChar);
					}
					proccessNormalMode(keyChar);
					break;
				case COMMAND:
					proccessCommandMode(keyChar);
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	private void proccessCommandMode(char keyChar) {
		if(keyChar == GameKeys.ENTER) {
			boolean isJumpState = false;
			String cmd = cmdBar.getCommand();
			if(cmd.equals(":q")) {
				playControl.GameOver();
			}
			else if(cmd.equals(":h")) {
				playControl.help();
			}
			else if(cmd.equals(":p")) {
				playControl.pause();
			}
			else if(cmd.matches(":\\d+")) {
				if(mp.getCurrentMp() >= 300) {
					boolean isJump = map.moveLine(this, Integer.parseInt(cmd.substring(1)));
					if(isJump) {
						mp.minus(300);
						score.plus(30);
						cmdBar.clear();
					}
					else cmdBar.setErr("line number doesn't exist.");
				}
				else cmdBar.setErr("MP is not enough.");
			}
			else if(cmd.matches(":set score=\\d+")) {
				score.set(Integer.parseInt(cmdBar.getCommand().substring(11)));
			}
			if(!isJumpState)
				cmdBar.clear();
		}
		else if(keyChar == GameKeys.ESC) {
			cmdBar.clear();
		}
		else if(keyChar == GameKeys.BACKSPACE) {
			cmdBar.backSpace();
			if(cmdBar.getCommand().isEmpty()) {
				vim.leaveCommand();
			}
		}
		else {
			cmdBar.append(keyChar);
		}
	}

	private void proccessNormalMode(Character keyChar) {
				lastPosX = pos.x;
				lastPosY = pos.y;
				if(isFindCharState) {
					boolean isFind = false;
					for(int i = 0; i < repeatTime; ++i) {
						if(findCharDirection == 1)
							isFind = map.moveFindChar(pos, keyChar);
						else isFind = map.moveFindPreChar(pos, keyChar);
						if(!isFind) cmdBar.setErr(String.format("Cannot find char '%c'", keyChar));
						if(i > 0) {
							mp.minus(20);
						}
					}
					repeatTime = 1;
					isFindCharState = false;
					updateScreen();
					return;
				}
				if(isDeleteState && keyChar == GameKeys.d) {
					if(mp.getCurrentMp() >= 200) {
						isDeleteState = false;
						if(mp.getCurrentMp() - repeatTime * 200 < 0) {
							cmdBar.setErr("MP is not enough.");
							isDeleteState = false;
							repeatTime = 1;
							return ;
						}
						for(int i = 0; i < repeatTime; ++i) {
							mp.minus(200);
							map.deleteLineTrap(this);
							if(i < repeatTime - 1) {
								map.moveDown(this);
							}
						}
						updateScreen();
						repeatTime = 1;
						return ;
					}
				}
				if(repeatTime != 1) {
					if(mp.getCurrentMp() >= repeatTime * 20)
						mp.minus(repeatTime * 20);
					else {
						cmdBar.setErr("MP is not enough.");
						repeatTime = 1;
						return ;
					}
				}
				if(keyChar != GameKeys.d) {
					isDeleteState = false;
				}
				switch (keyChar) {
				case GameKeys.j:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[0]++;
						movePlusMP = 1;
						map.moveDown(this);
						animation.startDown();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.k:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[1]++;
						movePlusMP = 1;
						map.moveUp(this);
						animation.startUp();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.h:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[2]++;
						movePlusMP = 1;
						map.moveLeft(this);
						animation.startLeft();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.l:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[3]++;
						movePlusMP = 1;
						map.moveRight(this);
						animation.startRight();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.w:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[4]++;
						movePlusMP = 5;
						map.moveNextWord(this);
						animation.startJump();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.b:
					for(int i = 0; i < repeatTime; ++i) {
						statistic[5]++;
						movePlusMP = 5;
						map.movePreWord(this);
						animation.startJump();
					}
					repeatTime = 1;
					isStill = false;
					break;
				case GameKeys.NUM_0:
					statistic[6]++;
					movePlusMP = 5;
					map.moveLineBegin(pos);
					animation.startJump();
					break;
				case GameKeys.DOLLAR:
					statistic[7]++;
					movePlusMP = 5;
					map.moveLineEnd(pos);
					animation.startJump();
					isStill = false;
					break;
				case GameKeys.f:
					statistic[8]++;
					movePlusMP = 20;
					isFindCharState = true;
					findCharDirection = 1;
					break;
				case GameKeys.F:
					statistic[9]++;
					movePlusMP = 20;
					isFindCharState = true;
					findCharDirection = 0;
					break;
				case GameKeys.d:
					isDeleteState = true;
					break;
				case GameKeys.NUM_1:
				case GameKeys.NUM_2:
				case GameKeys.NUM_3:
				case GameKeys.NUM_4:
				case GameKeys.NUM_5:
				case GameKeys.NUM_6:
				case GameKeys.NUM_7:
				case GameKeys.NUM_8:
				case GameKeys.NUM_9:
					repeatTime = (int)keyChar - GameKeys.NUM_0;
					break;
				}
				updateScreen();

	}

	public void updateScreen() {
			map.updateScreenMap(pos);
			int screenStartRow = map.screenStartRow;
			int screenStartCol = map.screenStartCol;
			int row = pos.y - screenStartRow;
			int col = pos.x - screenStartCol;
			animation.setDstPos(105f + col * SQUARE_LENGTH, 630f - row * SQUARE_LENGTH);
			if(pos.x != lastPosX || pos.y != lastPosY) {
				score.plus(movePlusMP);
				mp.plus(movePlusMP);
				isStill = false;
			}
			cmdBar.setRow(pos.y);
			cmdBar.setCol(pos.x);
	}

	public void setMap(GameMap map) {
		this.map = map;
	}

	public void demage(int amount) {
		hp.minus(amount);
	}

	public boolean isDead() {
		return (hp.getCurrentHp() <= 0);
	}

	public void convertPos(Position screenPos) {
		setPosition(55f + screenPos.getScreenX(),630f + screenPos.getScreenY());
	}

	@Override
	protected void positionChanged() {
		//sprite.setPosition(getX(), getY());
		super.positionChanged();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		animation.draw();
	}

	public void stillOverTime(int level) {
			if(!isStill) {
				isStill = true;
				lastStaticTime = 0f;
			}
			else {
				lastStaticTime += Gdx.graphics.getDeltaTime();
			}

		switch (level) {
		case 1:
			stillDemage(5f, 30);
			break;
		case 2:
			stillDemage(4f, 50);
			break;
		case 3:
			stillDemage(3f, 80);
			break;
		case 4:
			stillDemage(2f, 100);
			break;
		case 5:
			stillDemage(1.5f, 200);
			break;
		}
	}

	private void stillDemage(float timeLimitSec, int demageAmount) {
			if(lastStaticTime >= timeLimitSec) {
				hp.minus(demageAmount);
				lastStaticTime = System.currentTimeMillis();
				cmdBar.setErr("<KEEP MOVING, OR YOU WILL DIE>");
				stillDemage.play();
				lastStaticTime = 0f;
			}
	}

	@Override
	public void act(float delta) {
		// TODO Auto-generated method stub
		super.act(delta);
	}

	@Override
	public void draw(SpriteBatch batch) {
		//leave empty
	}

	@Override
	public void update() {
		accumulateTime += Gdx.graphics.getDeltaTime();
		if(accumulateTime > 10) accumulateTime = 0f;
		int collision_type = map.isCollision(this);
		if(collision_type != 0) {
			if(!isImmortal) {
				map.collision(this);
				isImmortal = true;
				accumulateTime = 0f;
			}
			if(collision_type == 1) {
				map.collision(this);
			}
		}
		if(accumulateTime >= 2f){
			isImmortal = false;
		}
	}

	@Override
	public int getRow() {
		return pos.y;
	}

	@Override
	public int getCol() {
		return pos.x;
	}

	@Override
	public void setRow(int row) {
		pos.y = row;
	}

	@Override
	public void setCol(int col) {
		pos.x = col;
	}

}
