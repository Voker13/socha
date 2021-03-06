package sc.shared

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import sc.protocol.responses.ProtocolMessage

/** Nachricht, die zu Beginn eines Spiels an einen Client geschickt wird, um ihm seine Spielerfarbe mitzuteilen.  */
@Suppress("DataClassPrivateConstructor")
@XStreamAlias(value = "welcomeMessage")
data class WelcomeMessage
private constructor(
        @XStreamAsAttribute private val color: String
): ProtocolMessage {
    
    constructor(color: PlayerColor): this(color.toString().toLowerCase())
    
    val playerColor: PlayerColor
        get() = PlayerColor.valueOf(color.toUpperCase())
    
}
