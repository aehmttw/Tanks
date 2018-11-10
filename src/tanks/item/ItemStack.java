package tanks.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ItemStack {
	@Getter
	private final Item item;
	@Getter
	private final int count;
}
