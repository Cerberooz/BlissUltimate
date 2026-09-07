package cerberooz.cerberooz.BlissUltimate;

import java.util.Map;
import java.util.UUID;

record EnergySnapshot(Map<UUID, Integer> energies, Map<UUID, String> lastKnownNames) {}
