package sc.server.plugins;

import sc.api.plugins.IMove;
import sc.shared.PlayerColor;

public class TestMove implements IMove {

  public int value;

  public TestMove(int i) {
    this.value = i;
  }

  public void perform(TestGameState state) {
    state.setState(this.value);
    state.setTurn(state.getTurn() + 1);
    state.setCurrentPlayer(state.getCurrentPlayer() == PlayerColor.RED ? PlayerColor.BLUE : PlayerColor.RED);
  }

}
