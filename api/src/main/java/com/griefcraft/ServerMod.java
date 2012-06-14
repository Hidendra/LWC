/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft;

public enum ServerMod {

    /**
     * TODO -- not even released
     * Official Minecraft Mod API
     */
    MCAPI (Game.MINECRAFT),

    /**
     * Bukkit - http://bukkit.org
     */
    BUKKIT (Game.MINECRAFT),

    /**
     * TODO -- not implemented
     * Spout - http://spout.org
     */
    SPOUT (Game.MINECRAFT),

    /**
     * TODO -- partially implemented
     * Canary, the hMod successfor - http://canarymod.net
     */
    CANARY (Game.MINECRAFT),

    /**
     * TDSM - http://tdsm.org
     */
    TDSM (Game.TERRARIA),

    /**
     * TODO -- not implemented, TBD after testing with TDSM
     * TShock - http://tshock.co
     */
    TSHOCK (Game.TERRARIA) ;

    /**
     * The game this mod is for
     */
    private Game game;

    ServerMod(Game game) {
        this.game = game;
    }

    /**
     * Get the game this mod is for
     * @return
     */
    public Game game() {
        return game;
    }


}
