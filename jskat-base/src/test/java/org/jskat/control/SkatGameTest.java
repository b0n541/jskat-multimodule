/**
 * Copyright (C) 2020 Jan Schäfer (jansch@users.sourceforge.net)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jskat.control;

import com.google.common.eventbus.EventBus;
import org.jskat.AbstractJSkatTest;
import org.jskat.ai.rnd.AIPlayerRND;
import org.jskat.ai.test.*;
import org.jskat.data.*;
import org.jskat.data.GameAnnouncement.GameAnnouncementFactory;
import org.jskat.data.SkatGameData.GameState;
import org.jskat.data.SkatTableOptions.RuleSet;
import org.jskat.gui.UnitTestView;
import org.jskat.player.JSkatPlayer;
import org.jskat.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link SkatGame}
 */
public class SkatGameTest extends AbstractJSkatTest {

    private static final String TABLE_NAME = "Table 1";
    private final Random random = new Random();

    @BeforeEach
    public void setUp() {
        JSkatOptions.instance().resetToDefault();
        JSkatEventBus.TABLE_EVENT_BUSSES.put(TABLE_NAME, new EventBus());
    }

    @Test
    public void testContra_NoContraActivatedInOptions() {
        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new ContraReCallingTestPlayer(),
                new ContraReCallingTestPlayer(),
                new ContraReCallingTestPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertFalse(summary.isContra());
        assertFalse(summary.isRe());
    }

    private static void runGame(SkatGame game) {
        try {
            CompletableFuture.runAsync(() -> game.run()).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testContra_ContraActivatedInOptions() {

        JSkatOptions options = JSkatOptions.instance();
        options.setRules(RuleSet.PUB);
        options.setPlayContra(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new ContraReCallingTestPlayer(),
                new ContraReCallingTestPlayer(),
                new ContraReCallingTestPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertTrue(summary.isContra());
        assertTrue(summary.isRe());
    }

    /**
     * When no player bids, game is passed in
     */
    @Test
    public void testPassIn_NoBids() {

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                getNoBiddingPlayer(), getNoBiddingPlayer(),
                getNoBiddingPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.PASSED_IN);

        SkatGameResult result = game.getGameResult();
        assertFalse(result.isWon());
        assertThat(result.getGameValue()).isEqualTo(0);
    }

    /**
     * When no player bids, game is passed in
     */
    @Test
    public void testPassIn_NoBidsMockito() {

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                getNoBiddingPlayer(), getNoBiddingPlayer(),
                getNoBiddingPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.PASSED_IN);

        SkatGameResult result = game.getGameResult();
        assertFalse(result.isWon());
        assertThat(result.getGameValue()).isEqualTo(0);
    }

    private static JSkatPlayer getNoBiddingPlayer() {
        JSkatPlayer player = mock(JSkatPlayer.class);
        when(player.bidMore(anyInt())).thenReturn(-1);
        when(player.holdBid(anyInt())).thenReturn(false);
        return player;
    }

    /**
     * When no player bids, game is passed in even the options for playing ramsch
     * are set but the active ruleset is ISPA rules
     */
    @Test
    public void testPassIn_NoBids2() {

        JSkatOptions options = JSkatOptions.instance();
        options.setPlayRamsch(true);
        options.setRamschEventNoBid(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                getNoBiddingPlayer(), getNoBiddingPlayer(),
                getNoBiddingPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.PASSED_IN);

        SkatGameResult result = game.getGameResult();
        assertFalse(result.isWon());
        assertThat(result.getGameValue()).isEqualTo(0);
    }

    /**
     * When no player bids and pub rules are acitvated, ramsch is played
     */
    @Test
    public void testRamsch_NoBids() {

        JSkatOptions options = JSkatOptions.instance();
        options.setRules(RuleSet.PUB);
        options.setPlayRamsch(true);
        options.setRamschEventNoBid(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new RamschTestPlayer(), new RamschTestPlayer(),
                new RamschTestPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.RAMSCH);

        SkatGameResult result = game.getGameResult();
        assertFalse(result.isWon());
        assertTrue(result.getGameValue() < 0);
    }

    /**
     * Forced ramsch game
     */
    @Test
    public void testRamsch_Forced() {

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.FORCED_RAMSCH,
                new RamschTestPlayer(), new RamschTestPlayer(),
                new RamschTestPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.RAMSCH);

        SkatGameResult result = game.getGameResult();
        assertFalse(result.isWon());
        assertTrue(result.getGameValue() < 0);
    }

    /**
     * Forced ramsch game, grand hand is played if fore hand wants to
     */
    @Test
    public void testRamsch_ForcedForeHandGrandHand() {

        RamschTestPlayer grandHandPlayer = new RamschTestPlayer();
        grandHandPlayer.setPlayGrandHand(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.FORCED_RAMSCH,
                grandHandPlayer, new AIPlayerRND(), new AIPlayerRND());
        game.setView(new UnitTestView());

        runGame(game);

        assertThat(game.getDeclarer()).isEqualTo(Player.FOREHAND);
        GameAnnouncement announcement = game.getGameAnnouncement();
        assertThat(announcement.getGameType()).isEqualTo(GameType.GRAND);
        assertTrue(announcement.isHand());

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.GRAND);
    }

    /**
     * Forced ramsch game, grand hand is played if middle hand wants to
     */
    @Test
    public void testRamsch_ForcedMiddleHandGrandHand() {

        RamschTestPlayer grandHandPlayer = new RamschTestPlayer();
        grandHandPlayer.setPlayGrandHand(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.FORCED_RAMSCH,
                new RamschTestPlayer(), grandHandPlayer, new RamschTestPlayer());
        game.setView(new UnitTestView());

        runGame(game);

        assertThat(game.getDeclarer()).isEqualTo(Player.MIDDLEHAND);
        GameAnnouncement announcement = game.getGameAnnouncement();
        assertThat(announcement.getGameType()).isEqualTo(GameType.GRAND);
        assertTrue(announcement.isHand());

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.GRAND);
    }

    /**
     * Forced ramsch game, grand hand is played if rear hand wants to
     */
    @Test
    public void testRamsch_ForcedRearHandGrandHand() {

        RamschTestPlayer grandHandPlayer = new RamschTestPlayer();
        grandHandPlayer.setPlayGrandHand(true);

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.FORCED_RAMSCH,
                new RamschTestPlayer(), new RamschTestPlayer(), grandHandPlayer);
        game.setView(new UnitTestView());

        runGame(game);

        assertThat(game.getDeclarer()).isEqualTo(Player.REARHAND);
        GameAnnouncement announcement = game.getGameAnnouncement();
        assertThat(announcement.getGameType()).isEqualTo(GameType.GRAND);
        assertTrue(announcement.isHand());

        GameSummary summary = game.getGameSummary();
        assertThat(summary.getGameType()).isEqualTo(GameType.GRAND);
    }

    @Test
    public void exceptionFromPlayerDuringGame() {
        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new ExceptionTestPlayer(), new ExceptionTestPlayer(),
                new ExceptionTestPlayer());
        game.setView(new UnitTestView());

        randomGameAnnouncement(game);

        runGame(game);

        SkatGameResult gameResult = game.getGameResult();
        if (!nullGameEndedPreliminary(game)) {
            assertTrue(gameResult.isSchwarz());
        }
    }

    private static boolean nullGameEndedPreliminary(SkatGame game) {
        // in Null games the game might have ended preliminary before all tricks
        // have been played
        return GameType.NULL.equals(game.getGameAnnouncement().getGameType()) && game
                .getGameSummary().getTricks().size() < 10;
    }

    private void randomGameAnnouncement(SkatGame game) {
        CardDeck deck = new CardDeck();
        game.setCardDeck(deck);
        game.dealCards();
        game.setDeclarer(Player.values()[random.nextInt(Player.values().length)]);
        GameAnnouncementFactory factory = GameAnnouncement.getFactory();
        factory.setGameType(getRandomGameType());
        game.setGameAnnouncement(factory.getAnnouncement());
        game.setGameState(GameState.TRICK_PLAYING);
    }

    private GameType getRandomGameType() {
        return Arrays.asList(GameType.GRAND, GameType.CLUBS, GameType.SPADES,
                GameType.HEARTS, GameType.DIAMONDS, GameType.NULL,
                GameType.RAMSCH).get(random.nextInt(6));
    }

    @Test
    public void playerPlaysNonPossessingCard() {
        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new PlayNonPossessingCardTestPlayer(),
                new PlayNonPossessingCardTestPlayer(),
                new PlayNonPossessingCardTestPlayer());
        game.setView(new UnitTestView());

        randomGameAnnouncement(game);

        runGame(game);

        SkatGameResult gameResult = game.getGameResult();
        assertTrue(gameResult.isSchwarz());
        assertThat(gameResult.getFinalDeclarerPoints() + gameResult.getFinalOpponentPoints()).isEqualTo(120);
    }

    @Test
    public void playerPlaysNotAllowedCard() {
        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new PlayNotAllowedCardTestPlayer(),
                new PlayNotAllowedCardTestPlayer(),
                new PlayNotAllowedCardTestPlayer());
        game.setView(new UnitTestView());

        randomGameAnnouncement(game);

        runGame(game);

        SkatGameResult gameResult = game.getGameResult();
        assertTrue(gameResult.isSchwarz());
        assertThat(gameResult.getFinalDeclarerPoints() + gameResult.getFinalOpponentPoints()).isEqualTo(120);
    }

    @Test
    public void testCompleteGame() {
        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD,
                new AIPlayerRND(), new AIPlayerRND(), new AIPlayerRND());
        game.setView(new UnitTestView());

        randomGameAnnouncement(game);

        runGame(game);

        Player declarer = game.getDeclarer();
        SkatGameResult result = game.getGameResult();

        if (declarer != null && result.getGameValue() > 0) {
            assertThat(result.getFinalDeclarerPoints() + result.getFinalOpponentPoints()).isEqualTo(120);
            GameSummary summary = game.getGameSummary();

            Map<Player, Integer> playerPointsInTricks = new HashMap();
            playerPointsInTricks.put(Player.FOREHAND, 0);
            playerPointsInTricks.put(Player.MIDDLEHAND, 0);
            playerPointsInTricks.put(Player.REARHAND, 0);

            for (Trick trick : summary.getTricks()) {

                Integer playerPoints = playerPointsInTricks.get(trick.getTrickWinner());
                if (playerPoints == null) {
                    playerPoints = 0;
                }
                playerPoints = playerPoints + trick.getValue();
                playerPointsInTricks.put(trick.getTrickWinner(), playerPoints);
            }

            assertThat(result.getFinalOpponentPoints())
                    .isEqualTo(playerPointsInTricks.get(declarer.getRightNeighbor())
                            + playerPointsInTricks.get(declarer.getLeftNeighbor()));
        }
    }

    @Test
    public void testPredefinedCardPlaying() {
        UnitTestPlayer foreHand = new UnitTestPlayer();
        foreHand.setCardsToPlay(
                Arrays.asList(Card.C7, Card.SJ, Card.C9, Card.H8, Card.DQ, Card.D7, Card.CT, Card.DK, Card.CA, Card.CK));

        UnitTestPlayer middleHand = new UnitTestPlayer();
        middleHand.setCardsToPlay(
                Arrays.asList(Card.CQ, Card.C8, Card.SQ, Card.ST, Card.S9, Card.HT, Card.HA, Card.DA, Card.HK, Card.D9));

        UnitTestPlayer rearHand = new UnitTestPlayer();
        rearHand.setCardsToPlay(
                Arrays.asList(Card.DJ, Card.CJ, Card.HJ, Card.SA, Card.S8, Card.H7, Card.H9, Card.D8, Card.HQ, Card.DT));

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD, foreHand, middleHand, rearHand);
        game.setView(new UnitTestView());

        CardDeck deck = new CardDeck(
                "SJ CA CT CK C9 C7 H8 DK DQ D7",
                "CQ C8 ST SQ S9 HA HT HK DA D9",
                "CJ HJ DJ SA S8 HQ H9 H7 DT D8",
                "SK S7");
        game.setCardDeck(deck);
        game.dealCards();
        game.setDeclarer(Player.MIDDLEHAND);
        GameAnnouncementFactory factory = GameAnnouncement.getFactory();
        factory.setGameType(GameType.CLUBS);
        game.setGameAnnouncement(factory.getAnnouncement());
        game.setGameState(GameState.TRICK_PLAYING);

        runGame(game);

        SkatGameResult result = game.getGameResult();
        assertThat(result.getFinalDeclarerPoints()).isEqualTo(32);
        assertThat(result.getFinalOpponentPoints()).isEqualTo(88);
    }

    /**
     * Tests the fix for Issue #33:<br>
     * <p>
     * https://github.com/b0n541/jskat-multimodule/issues/33
     */
    @Test
    public void testGameResultIssue33() {

        UnitTestPlayer foreHand = new UnitTestPlayer();
        foreHand.setCardsToPlay(
                Arrays.asList(Card.SJ, Card.CJ, Card.D8, Card.SA, Card.C7, Card.ST, Card.C9, Card.S7, Card.S9, Card.S8));

        UnitTestPlayer middleHand = new UnitTestPlayer();
        middleHand.setCardsToPlay(
                Arrays.asList(Card.SQ, Card.SK, Card.DA, Card.DT, Card.CK, Card.H8, Card.DQ, Card.DK, Card.HQ, Card.HT));

        UnitTestPlayer rearHand = new UnitTestPlayer();
        rearHand.setCardsToPlay(
                Arrays.asList(Card.DJ, Card.HJ, Card.D7, Card.H7, Card.CT, Card.HA, Card.CQ, Card.C8, Card.H9, Card.HK));

        SkatGame game = new SkatGame(TABLE_NAME, GameVariant.STANDARD, foreHand, middleHand, rearHand);
        game.setView(new UnitTestView());

        CardDeck deck = new CardDeck(
                "CJ SJ SA ST S9 S8 S7 C9 C7 D8",
                "SK SQ CK HT HQ H8 DA DT DK DQ",
                "HJ DJ CT CQ C8 HA HK H9 H7 D7",
                "D9 CA");
        game.setCardDeck(deck);
        game.dealCards();
        game.setDeclarer(Player.FOREHAND);
        GameAnnouncementFactory factory = GameAnnouncement.getFactory();
        factory.setGameType(GameType.SPADES);
        game.setGameAnnouncement(factory.getAnnouncement());
        game.setGameState(GameState.TRICK_PLAYING);

        runGame(game);

        SkatGameResult result = game.getGameResult();
        assertThat(result.getFinalDeclarerPoints()).isEqualTo(89);
        assertThat(result.getFinalOpponentPoints()).isEqualTo(31);
        assertFalse(result.isSchneider());
        assertThat(result.getGameValue()).isEqualTo(33);
    }
}