package sc.api.plugins

import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamOmitField
import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.shared.PlayerColor

abstract class TwoPlayerGameState<P : Player>(
        /** Farbe des Startspielers. */
        @XStreamAsAttribute val startPlayerColor: PlayerColor = PlayerColor.RED
) : IGameState {

    abstract val red: P
    abstract val blue: P
    abstract val board: IBoard

    @XStreamOmitField
    private val logger = LoggerFactory.getLogger(TwoPlayerGameState::class.java)
    
    /** Farbe des Spielers, der aktuell am Zug ist. */
    abstract val currentPlayerColor: PlayerColor

    /** Liste der Spieler. Reihenfolge: RED, BLUE */
    val players: List<P>
        get() = listOf(red, blue)

    /** Der Spieler, der momentan am Zug ist. */
    val currentPlayer: P
        get() = getPlayer(currentPlayerColor)

    /** Der Spieler, der momentan nicht am Zug ist. */
    val otherPlayer: P
        get() = getPlayer(otherPlayerColor)

    /** Farbe des Spielers, der momentan nicht am Zug ist. */
    val otherPlayerColor: PlayerColor
        get() = currentPlayerColor.opponent()

    /** Der Spieler, der das Spiel begonnen hat. */
    val startPlayer: P
        get() = getPlayer(startPlayerColor)

    /** Die Namen der beiden Spieler. */
    val playerNames: Array<String>
        get() = arrayOf(red.displayName, blue.displayName)

    /** Letzter getaetigter Zug. */
    abstract val lastMove: IMove?

    fun getOpponent(player: P) =
            getPlayer(player.color.opponent())

    fun getPlayer(color: PlayerColor): P =
            when(color) {
                PlayerColor.RED -> red
                PlayerColor.BLUE -> blue
            }
    
    /** Calculates the color of the current player from the [turn] and the [startPlayerColor].
     * Based on the assumption that the current player switches every turn. */
    protected fun currentPlayerFromTurn() =
            if(turn.rem(2) == 0) startPlayerColor else startPlayerColor.opponent()

    /** Gibt die angezeigte Punktzahl des Spielers zurueck. */
    abstract fun getPointsForPlayer(playerColor: PlayerColor): Int

    override fun toString() =
            "GameState(turn=$turn,currentPlayer=${currentPlayer.color})"

}