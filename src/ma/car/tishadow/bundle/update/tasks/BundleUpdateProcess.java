/**
 * Represents a list of states in a bundle update process, and each task can only stay at one of these states at the same time.
 */
package ma.car.tishadow.bundle.update.tasks;

public enum BundleUpdateProcess {
	CHECKED,
	DOWNLOADED,
	DECOMPRESSED,
	READY_FOR_APPLY,
	APPLYED,

	STARTING,
	DOWNLOADING,
	APPLYING
}