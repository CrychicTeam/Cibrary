package org.pickaid.pibrary.tools.compat.kuebjs.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.tools.registry.RegistryHelper;

import java.util.Arrays;

public interface ItemComponent {
    RecipeComponent<Item> ITEM = new RecipeComponent<Item>() {
        @Override
        public Class<?> componentClass() {
            return Item.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, Item value) {
            return new JsonPrimitive(RegistryHelper.getItem(value).toString());
        }

        @Override
        public Item read(RecipeJS recipe, Object from) {
            if (from instanceof String string) {
                return ForgeRegistries.ITEMS.getValue(new ResourceLocation(string)) == null ? ItemStack.EMPTY.getItem() : ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
            } else if (from instanceof ItemStack stack) {
                return stack.getItem();
            } else if (from instanceof Ingredient ingredient) {
                return Arrays.stream(ingredient.getItems()).findFirst().get().getItem();
            }
            return ItemStack.EMPTY.getItem();
        }
    };
}
