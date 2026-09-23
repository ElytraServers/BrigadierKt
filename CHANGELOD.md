# Changelog

## [1.2.0]

### Added

- Added `argumentContext` that provides a value getter in the block.
- Added `executesContext` that provides `CommandContext<S>` as context in the block.
- Added `ExecuteScope`.

### Removed

- Removed `via`.
- Removed `registerCommandContextValueProvider` for custom getters.

### Changed

- operation extension `getValue` for `CommandContext<S>` now works like built-in argument types that cast the existing
  value to the desired type directly. See `StringArgumentType#getString(CommandContext<S>, String)` for example.
- Deprecated `executesUnit`. Used `executesKt` instead; use `result(Int)` to set a non-default result.
