/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.serverpackets.sessionzones.TimedHuntingZoneList;

/**
 * @author Mobius
 */
public class AddHuntingTime extends AbstractEffect
{
	private final int _zoneId;
	private final long _time;
	
	public AddHuntingTime(StatSet params)
	{
		_zoneId = params.getInt("zoneId", 0);
		_time = params.getLong("time", 3600000);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		final PlayerInstance player = effected.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		long endTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + _zoneId, 0);
		if ((endTime > currentTime) && (((endTime - currentTime) + _time) >= Config.TIME_LIMITED_MAX_ADDED_TIME))
		{
			player.getInventory().addItem("AddHuntingTime effect refund", item.getId(), 1, player, false);
			player.sendMessage("You cannot exceed the time zone limit.");
			return;
		}
		
		if (player.isInTimedHuntingZone(_zoneId))
		{
			endTime = _time + player.getTimedHuntingZoneRemainingTime();
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_RESET_TIME + _zoneId, currentTime + endTime);
			player.startTimedHuntingZone(_zoneId, endTime);
		}
		else
		{
			if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
			{
				endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
			}
			else if (endTime < currentTime)
			{
				endTime = currentTime;
			}
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_RESET_TIME + _zoneId, endTime + _time);
		}
		
		player.sendPacket(new TimedHuntingZoneList(player));
	}
}
