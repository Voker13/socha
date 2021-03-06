package sc.plugin2020

import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import sc.plugin2020.util.*
import sc.plugin2020.util.TestJUnitUtil.assertThrows
import sc.shared.InvalidMoveException
import sc.shared.PlayerColor
import java.security.InvalidParameterException


class GamePlayTest {
    
    private lateinit var game: Game
    private lateinit var state: GameState
    
    @Before
    fun beforeEveryTest() {
        game = Game()
        state = game.gameState
    }
    
    @Test
    fun boardCreationTest() {
        val board = Board()
        assertNotNull(board.getField(0, 0, 0))
        assertEquals(board, board.clone())
    }
    
    @Test
    fun obstructedCreationTest() {
        val board = Board()
        assertEquals(3, board.fields.filter { it.isObstructed }.size)
    }
    
    @Test
    fun invalidBoardStringTest() {
        assertThrows(InvalidParameterException::class.java) {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     XY----------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
        }
        assertThrows(InvalidParameterException::class.java) {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     BY----------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
        }
    }
    
    @Test
    fun boardCloneTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     ------------" +
                "    --------------" +
                "   ----BBOOBB------" +
                "  ----RBRS--BS------" +
                " ------RBRQBQ--------" +
                "------------RG--------" +
                " ----OO--------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        assertEquals(state.board, state.board.clone())
    }
    
    @Ignore
    @Test
    fun onlyEndAfterRoundTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     RB----------" +
                "    --------------" +
                "   ----------------" +
                "  ------------------" +
                " --------------------" +
                "----------------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        run {
            //Move move = new Move();
            //GameRuleLogic.performMove(state, move);
        }
    }
    
    @Test
    fun redBeeSurroundedTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     RQBQ--------" +
                "    BB------------" +
                "   --BB------------" +
                "  ------------------" +
                " --------------------" +
                "----------------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        run {
            state.turn = 1
            val move = DragMove(CubeCoordinates(-1, 4), CubeCoordinates(0, 4))
            GameRuleLogic.performMove(state, move)
            assertTrue(GameRuleLogic.isBeeBlocked(state.board, PlayerColor.RED))
        }
    }
    
    @Test
    fun gameEndTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ----BBBB----------" +
                    " ----BBRQBB----------" +
                    "------BBBB------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertTrue(GameRuleLogic.isBeeBlocked(state.board, PlayerColor.RED))
        }
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ----BBBB----------" +
                    " ------RQBB----------" +
                    "------BBBB------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertFalse(GameRuleLogic.isBeeBlocked(state.board, PlayerColor.RED))
        }
    }
    
    @Test
    fun setMoveOnEmptyBoardTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(0, 0))
            GameRuleLogic.validateMove(state, move)
        }
    }
    
    @Test
    fun setMoveOutsideOfBoard() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(8, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun validSetMoveTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  --BG--------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(0, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun setMoveOfUnavailablePieceTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGBG--------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            state.getUndeployedPieces(PlayerColor.RED).clear()
            val move = SetMove(Piece(PlayerColor.RED, PieceType.ANT), CubeCoordinates(-4, 4))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun setMoveConnectionToSwarmTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGBG--------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val invalid1 = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(0, 0))
            assertThrows(InvalidMoveException::class.java) {
                GameRuleLogic.validateMove(state, invalid1)
                val invalid2 = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(-3, 4))
                assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid2) }
            }
            val valid1 = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(-4, 5))
            GameRuleLogic.validateMove(state, valid1)
        }
    }
    
    @Test
    fun setMoveNextToOpponentTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGBG--------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val invalid = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(-2, 4))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid) }
        }
    }
    
    @Test
    fun setMoveBlockedFieldTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGBGOO------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val invalid1 = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(-3, 4))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid1) }
            val invalid2 = SetMove(state.getUndeployedPieces(PlayerColor.RED)[0], CubeCoordinates(-1, 2))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid2) }
        }
    }
    
    @Test
    fun setMoveForceBeeTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGRGRG------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            state.turn = 6
            val setAnt = SetMove(Piece(PlayerColor.RED, PieceType.ANT), CubeCoordinates(-4, 5))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, setAnt) }
            val skip = SkipMove
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, skip) }
            val setBee = SetMove(Piece(PlayerColor.RED, PieceType.BEE), CubeCoordinates(-4, 5))
            GameRuleLogic.validateMove(state, setBee)
        }
    }
    
    @Test
    fun dragMoveNonexistentPieceTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 0), CubeCoordinates(0, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveOfSolePieceOnBoardTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state,
                    "     ------------" +
                            "    --------------" +
                            "   ----------------" +
                            "  ------------------" +
                            " --------------------" +
                            "----------RQ----------" +
                            " --------------------" +
                            "  ------------------" +
                            "   ----------------" +
                            "    --------------" +
                            "     ------------")
            val move = DragMove(CubeCoordinates(0, 0), CubeCoordinates(1, -1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveOntoOtherPieceTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------RQBQ--------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 0), CubeCoordinates(1, -1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveBeeRequiredTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------RBBG--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(0, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  --------BA--------" +
                    " --------RB----------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 0), CubeCoordinates(1, 0))
            GameRuleLogic.validateMove(state, move)
        }
    }
    
    @Test
    fun dragMoveBeeValidTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------RBBG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(-1, 1))
            GameRuleLogic.validateMove(state, move)
        }
    }
    
    @Test
    fun dragMoveBeeTooFarTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------RBBG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, -1), CubeCoordinates(2, -2))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveBeetleTooFarTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------RBBG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 0), CubeCoordinates(2, -1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveBeetleDisconnectTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BA----------" +
                    " --------RBBG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BA----------" +
                    " ------RARBBA--------" +
                    "--------BQRQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
            GameRuleLogic.validateMove(state, move)
        }
    }
    
    
    @Test
    fun dragMoveBeetleNoJumpTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   --------RG------" +
                    "  --------BG--RB----" +
                    " ----------RQBQ------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(3, -1), CubeCoordinates(3, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveBeetleClimbTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     ------------" +
                "    --------------" +
                "   ----------------" +
                "  --------RQ--------" +
                " --------RB----------" +
                "----------------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
        GameRuleLogic.validateMove(state, move)
    }
    
    @Test
    fun dragMoveGrasshopperValidTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BABB--------" +
                    " ------BQRBRG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(1, 0), CubeCoordinates(-2, 3))
            GameRuleLogic.validateMove(state, move)
            val move2 = DragMove(CubeCoordinates(1, 0), CubeCoordinates(1, 2))
            GameRuleLogic.validateMove(state, move2)
        }
    }
    
    @Test
    fun dragMoveGrasshopperOverEmptyFieldTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BABB--------" +
                    " ------BQRBRG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(1, 0), CubeCoordinates(-3, 4))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveGrasshopperToNeighbourTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BABB--------" +
                    " ------BQRBRG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(1, 0), CubeCoordinates(1, -1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveAntValidTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------RABB--------" +
                    " ------BQRBRG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 2), CubeCoordinates(1, 2))
            GameRuleLogic.validateMove(state, move)
            val move2 = DragMove(CubeCoordinates(0, 2), CubeCoordinates(0, -1))
            GameRuleLogic.validateMove(state, move2)
        }
    }
    
    @Test
    fun dragMoveAntAroundObstacleTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     ------------" +
                "    --------------" +
                "   ----------------" +
                "  --------OO--------" +
                " ------RQRA----------" +
                "--------OO------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(0, 2))
        assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        val move2 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
        assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move2) }
        val move3 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(0, 0))
        assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move3) }
    }
    
    @Test
    fun dragMoveAntIntoBlockedTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   --------RB------" +
                    "  ------RABB--BB----" +
                    " ------BQRBRGBB------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 1), CubeCoordinates(2, -1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveAntAroundBorderTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   RARQ------------" +
                    "  --BQ--------------" +
                    " --OO----------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(-2, 5), CubeCoordinates(-4, 5))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
            val move2 = DragMove(CubeCoordinates(-2, 5), CubeCoordinates(0, 5))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move2) }
            val move3 = DragMove(CubeCoordinates(-2, 5), CubeCoordinates(-2,3))
            GameRuleLogic.validateMove(state, move3)
        }
    }
    
    @Test
    fun dragMoveAntDisconnectFromSwarmTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------RABB--------" +
                    " ------BQ--RG--------" +
                    "----------RQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(0, 2), CubeCoordinates(-1, 1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, move) }
        }
    }
    
    @Test
    fun dragMoveSpiderTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ----------BB------" +
                    " --------RS--BS------" +
                    "--------RBRQBQ--------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val valid1 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(2, 1))
            GameRuleLogic.validateMove(state, valid1)
            val valid2 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(-2, 1))
            GameRuleLogic.validateMove(state, valid2)
            val invalid1 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid1) }
            val invalid2 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid2) }
            val invalid3 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(-1, 2))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid3) }
            val invalid4 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(0, 2))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid4) }
            val invalid5 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(3, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid5) }
            val invalid6 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(-1, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid6) }
        }
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BB--BB------" +
                    " ------RBRS--BS------" +
                    "--------RBRQBQ--------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val valid1 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(2, 1))
            GameRuleLogic.validateMove(state, valid1)
            val valid2 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(3, 0))
            GameRuleLogic.validateMove(state, valid2)
            val valid3 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 2))
            GameRuleLogic.validateMove(state, valid3)
            val valid4 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(0, 3))
            GameRuleLogic.validateMove(state, valid4)
            val invalid1 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 0))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid1) }
            val invalid2 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(1, 1))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid2) }
            val invalid3 = DragMove(CubeCoordinates(0, 1), CubeCoordinates(-1, 2))
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid3) }
        }
    }
    
    @Test
    fun dragMoveEdgeTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  --RBRGBGBB--------" +
                    " RQBGBSRS--BS--------" +
                    "--RS--RBRABQ----------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val move = DragMove(CubeCoordinates(-4, 5), CubeCoordinates(-3, 5))
            GameRuleLogic.validateMove(state, move)
        }
    }

    @Test
    fun performMoveTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     ------------" +
                "    --------------" +
                "   ----------------" +
                "  ------------------" +
                " --------------------" +
                "----------------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        val move = SetMove(Piece(PlayerColor.RED, PieceType.ANT), CubeCoordinates(1, 2, -3))
        GameRuleLogic.performMove(state, move)
        assertEquals(PieceType.ANT, state.board.getField(1, 2, -3).topPiece?.type)
    }
    
    @Test
    fun possibleMoveTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   --RBRGBGBB------" +
                    "  RQBGBSRS--BS------" +
                    " --RS--RBRABQ--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertFalse(GameRuleLogic.getPossibleMoves(state).isEmpty())
        }
    }
    
    @Test
    fun possibleSetMoveDestinationsTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   --RBRGBGBB------" +
                    "  RQBGBSRS--BS------" +
                    " --RS--RBRABQ--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertEquals(7, GameRuleLogic.getPossibleSetMoveDestinations(state.board, state.currentPlayerColor).size)
        }
    }
    
    @Test
    fun possibleSetMoveDestinationsSecondTurnTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------RBOO--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            state.turn = 1
            assertEquals(PieceType.values().size * 5, GameRuleLogic.getPossibleSetMoves(state).size)
        }
    }
    
    @Test
    fun possibleSetMoveDestinationsFirstTurnTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------------------" +
                    " ----------OO--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertEquals(PieceType.values().size * (Constants.FIELD_AMOUNT - 1), GameRuleLogic.getPossibleSetMoves(state).size)
        }
    }
    
    @Test
    fun possibleDragMovesTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  ------BB----------" +
                    " --------RQOO--------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            assertEquals(1, GameRuleLogic.getPossibleDragMoves(state).size)
        }
    }
    
    @Test
    fun skipMoveTest() {
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     ------------" +
                    "    --------------" +
                    "   ----------------" +
                    "  RGBG--------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val invalid = SkipMove
            assertThrows(InvalidMoveException::class.java) { GameRuleLogic.validateMove(state, invalid) }
        }
        run {
            TestGameUtil.updateGamestateWithBoard(state, "" +
                    "     RA----------" +
                    "    --BG----------" +
                    "   ----------------" +
                    "  ------------------" +
                    " --------------------" +
                    "----------------------" +
                    " --------------------" +
                    "  ------------------" +
                    "   ----------------" +
                    "    --------------" +
                    "     ------------")
            val valid = SkipMove
            GameRuleLogic.validateMove(state, valid)
            assertEquals(GameRuleLogic.getPossibleMoves(state), listOf(SkipMove))
        }
    }
    
    @Test
    fun undeployedPiecesCloneTest() {
        val redMove = SetMove(Piece(PlayerColor.RED, PieceType.BEE), CubeCoordinates(-5, 0, 5))
        GameRuleLogic.performMove(state, redMove);
        val clone = GameState(state)
        val blueMove = SetMove(Piece(PlayerColor.BLUE, PieceType.BEE), CubeCoordinates(-4, -1, 5))
        GameRuleLogic.validateMove(clone, blueMove)
    }
    
    @Test
    fun beetleToObstructedTest() {
        TestGameUtil.updateGamestateWithBoard(state, "" +
                "     RBOO--------" +
                "    --BGRQ--------" +
                "   ----------------" +
                "  ------------------" +
                " --------------------" +
                "----------------------" +
                " --------------------" +
                "  ------------------" +
                "   ----------------" +
                "    --------------" +
                "     ------------")
        val moveBeetle = DragMove(CubeCoordinates(0, 5), CubeCoordinates(1, 4))
        assertThrows(InvalidMoveException::class.java) {
            GameRuleLogic.performMove(state, moveBeetle)
        }
    }
    
}
