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
package handlers.skillconditionhandlers;

import org.l2jmobius.gameserver.enums.SkillConditionAffectType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.ISkillCondition;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Sdw
 */
public class OpEquipItemSkillCondition implements ISkillCondition
{
	private final int _itemId;
	private final SkillConditionAffectType _affectType;
	
	public OpEquipItemSkillCondition(StatSet params)
	{
		_itemId = params.getInt("itemId");
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class);
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				return !caster.getInventory().getItems(ItemInstance::isEquipped, i -> i.getId() == _itemId).isEmpty();
			}
			case TARGET:
			{
				if ((target != null) && target.isPlayer())
				{
					return !target.getActingPlayer().getInventory().getItems(ItemInstance::isEquipped, i -> i.getId() == _itemId).isEmpty();
				}
				break;
			}
			case BOTH:
			{
				if ((target != null) && target.isPlayer())
				{
					return !caster.getInventory().getItems(ItemInstance::isEquipped, i -> i.getId() == _itemId).isEmpty() && target.getActingPlayer().getInventory().getItems(ItemInstance::isEquipped, i -> i.getId() == _itemId).isEmpty();
				}
				break;
			}
		}
		return false;
	}
}