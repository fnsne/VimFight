/**
 *
 */
package vimControl.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Input.Keys;

import vimControl.VimControl;
import vimControl.VimMode;

/**
 * @author 楊舜宇
 * @since 2016/5/28
 */
public class TestVimMode {

	public VimControl vim;
	@Before
	public void setUp() throws Exception {
		vim = new VimControl();
	}

	@Test
	public void NormalToInsert() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.I);
		assertEquals(VimMode.INSERT, vim.getCurrentState());
	}

	@Test
	public void NormalTOCommand() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.SEMICOLON);
		assertEquals(VimMode.COMMAND, vim.getCurrentState());
	}

	@Test
	public void InsertTONormal() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.I);
		vim.inputKey(Keys.ESCAPE);
		assertEquals(VimMode.NORMAL, vim.getCurrentState());
	}

	@Test
	public void CommandTONormal() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.SEMICOLON);
		vim.inputKey(Keys.ESCAPE);
		assertEquals(VimMode.NORMAL, vim.getCurrentState());
	}

	@Test
	public void TestInsertMode() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.I);
		assertEquals(VimMode.INSERT, vim.getCurrentState());
		vim.inputKey(Keys.I);
		vim.inputKey(Keys.A);
		vim.inputKey(Keys.K);
		assertEquals(VimMode.INSERT, vim.getCurrentState());
		vim.inputKey(Keys.O);
		vim.inputKey(Keys.D);
		assertEquals(VimMode.INSERT, vim.getCurrentState());
		vim.inputKey(Keys.ESCAPE);
		assertEquals(VimMode.NORMAL, vim.getCurrentState());
	}
	@Test
	public void TestCommandMode() {
		VimControl vim = new VimControl();
		vim.inputKey(Keys.SEMICOLON);
		assertEquals(VimMode.COMMAND, vim.getCurrentState());
		vim.inputKey(Keys.I);
		vim.inputKey(Keys.A);
		vim.inputKey(Keys.K);
		vim.inputKey(Keys.ESCAPE);
		assertEquals(VimMode.NORMAL, vim.getCurrentState());
		vim.inputKey(Keys.SEMICOLON);
		assertEquals(VimMode.COMMAND, vim.getCurrentState());
		vim.inputKey(Keys.O);
		vim.inputKey(Keys.L);
		vim.inputKey(Keys.P);
		vim.inputKey(Keys.A);
		assertEquals(VimMode.COMMAND, vim.getCurrentState());
		vim.inputKey(Keys.ENTER);
		assertEquals(VimMode.NORMAL, vim.getCurrentState());
	}
}