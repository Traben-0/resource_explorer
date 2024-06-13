package traben.resource_explorer.explorer.display.detail.entries;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import traben.resource_explorer.explorer.display.resources.entries.ResourceFileEntry;

class SoundPlayer implements SoundInstance {

    private final String id;
    private final Sound sound;

    SoundPlayer(ResourceFileEntry fileEntry) {
        id = "re_" + fileEntry.getDisplayName() + "2";
        sound = new Sound(Identifier.of("re_" + fileEntry.getDisplayName()), (a) -> 1, (a) -> 1, 1, Sound.RegistrationType.FILE, true, true, 1) {
            @Override
            public Identifier getLocation() {
                return fileEntry.identifier;
            }
        };
    }

    @Override
    public Identifier getId() {
        return Identifier.of(id);
    }

    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return new WeightedSoundSet(getId(), "wat");
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public SoundCategory getCategory() {
        return SoundCategory.MASTER;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 1;
    }

    @Override
    public float getPitch() {
        return 1;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }
}
