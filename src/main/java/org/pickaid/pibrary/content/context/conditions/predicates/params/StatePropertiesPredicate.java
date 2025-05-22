package org.pickaid.pibrary.content.context.conditions.predicates.params;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record StatePropertiesPredicate(List<PropertyMatcher> properties)  {
	public static final Codec<StatePropertiesPredicate> CODEC = PropertyMatcher.CODEC.listOf().xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
	public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(List.of());
	
	
	public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> properties, S targetProperty) {
		for (PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
			if (!statepropertiespredicate$propertymatcher.match(properties, targetProperty)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean matches(BlockState state) {
		return this.matches(state.getBlock().getStateDefinition(), state);
	}
	
	public boolean matches(FluidState state) {
		return this.matches(state.getType().getStateDefinition(), state);
	}
	
	public Optional<String> checkState(StateDefinition<?, ?> state) {
		for (PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
			Optional<String> optional = statepropertiespredicate$propertymatcher.checkState(state);
			if (optional.isPresent()) {
				return optional;
			}
		}
		
		return Optional.empty();
	}
	
	record PropertyMatcher(String name, ValueMatcher valueMatcher) {
		public static final Codec<PropertyMatcher> CODEC = Codec.pair(Codec.STRING, ValueMatcher.CODEC).xmap(pair -> new PropertyMatcher(pair.getFirst(), pair.getSecond()), propertyMatcher -> new Pair<>(propertyMatcher.name(), propertyMatcher.valueMatcher()));
		
		public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> properties, S propertyToMatch) {
			Property<?> property = properties.getProperty(this.name);
			return property != null && this.valueMatcher.match(propertyToMatch, property);
		}
		
		public Optional<String> checkState(StateDefinition<?, ?> state) {
			Property<?> property = state.getProperty(this.name);
			return property != null ? Optional.empty() : Optional.of(this.name);
		}
	}
	
	interface ValueMatcher {
		Codec<ValueMatcher> CODEC = Codec.either(ExactMatcher.CODEC, RangedMatcher.CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), matcher -> {
			if (matcher instanceof ExactMatcher exactMatcher) {
				return Either.left(exactMatcher);
			} else if (matcher instanceof RangedMatcher rangedMatcher) {
				return Either.right(rangedMatcher);
			} else {
				throw new UnsupportedOperationException();
			}
		});
		
		<T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property);
	}
	
	record ExactMatcher(String value) implements ValueMatcher {
		public static final Codec<ExactMatcher> CODEC = Codec.STRING.xmap(ExactMatcher::new, ExactMatcher::value);
		
		@Override
		public <T extends Comparable<T>> boolean match(StateHolder<?, ?> state, Property<T> property) {
			T t = state.getValue(property);
			Optional<T> optional = property.getValue(this.value);
			return optional.isPresent() && t.compareTo(optional.get()) == 0;
		}
	}
	
	record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements ValueMatcher {
		public static final Codec<RangedMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::minValue),
				Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::maxValue)
		).apply(instance, RangedMatcher::new));
		
		@Override
		public <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property) {
			T t = stateHolder.getValue(property);
			if (this.minValue.isPresent()) {
				Optional<T> optional = property.getValue(this.minValue.get());
				if (optional.isEmpty() || t.compareTo(optional.get()) < 0) {
					return false;
				}
			}
			
			if (this.maxValue.isPresent()) {
				Optional<T> optional1 = property.getValue(this.maxValue.get());
                return optional1.isPresent() && t.compareTo(optional1.get()) <= 0;
			}
			
			return true;
		}
	}
}
