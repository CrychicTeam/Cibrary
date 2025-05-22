package org.pickaid.pibrary.content.context.conditions;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import org.pickaid.pibrary.content.context.Context;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.function.Predicate;

/**
 * 谓词
 *
 * <p>JSON格式如下</p>
 * <pre>
 *     {
 *         "condition":"",
 *         其他部分, 由具体类型决定
 *     }
 * </pre>
 *
 * <ul>
 *     <li><code>condition</code>: 谓词类型</li>
 * </ul>
 */
public interface ContextPredicate extends Predicate<Context> {
	/**
	 * 这里将允许任何类型的条件, 例如不仅限于位置检查{@link LocationCheck}, 具体判断依然取决于上下文参数
	 */
	Codec<ContextPredicate> DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.CONTEXT_PREDICATE_TYPE.get().getCodec()).dispatch("condition", ContextPredicate::type, ContextConditionType::codec);
	
	ContextConditionType<?> type();
	
}
