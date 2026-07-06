import SwiftUI
import BanjenCore

struct SettingsSheet: View {
    @Bindable var viewModel: EarViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Instrument + tuning dropdowns + share
                SelectorRow(viewModel: viewModel, dismiss: { dismiss() })

                // Divider
                Rectangle()
                    .fill(AppColors.divider)
                    .frame(height: 1)
                    .padding(.vertical, 16)

                // Reference pitch control
                PitchControlRow(viewModel: viewModel)

                Spacer(minLength: 24)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
        }
        .background(AppColors.settingsBackground)
    }
}

// MARK: - SelectorRow

private struct SelectorRow: View {
    @Bindable var viewModel: EarViewModel
    let dismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Instrument dropdown
            DropdownSelector(
                label: viewModel.currentInstrument.name,
                items: ALL_INSTRUMENTS.map { $0.name },
                onSelected: { newIndex in
                    dismiss()
                    viewModel.selectInstrument(newIndex)
                }
            )

            // Tuning dropdown
            DropdownSelector(
                label: viewModel.currentTuning.name,
                items: viewModel.currentInstrument.tunings.map { $0.name },
                onSelected: { newIndex in
                    dismiss()
                    viewModel.selectTuning(newIndex)
                }
            )

            // Share button
            ShareLink(item: viewModel.shareText()) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 20))
                    .foregroundColor(AppColors.accent)
                    .frame(width: 40, height: 40)
            }
            .accessibilityLabel(String(localized: "share_tuning"))
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 16)
    }
}

// MARK: - DropdownSelector

private struct DropdownSelector: View {
    let label: String
    let items: [String]
    let onSelected: (Int) -> Void

    @State private var expanded = false

    var body: some View {
        Menu {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                Button(item) { onSelected(index) }
            }
        } label: {
            HStack(spacing: 4) {
                Text(label)
                    .font(.body.weight(.medium))
                    .foregroundColor(AppColors.dropdownLabel)
                Image(systemName: "chevron.down")
                    .font(.caption)
                    .foregroundColor(AppColors.dropdownLabel)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(AppColors.pillBg)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(AppColors.pillBorder, lineWidth: 1))
        }
    }
}

// MARK: - PitchControlRow

private struct PitchControlRow: View {
    @Bindable var viewModel: EarViewModel

    var body: some View {
        HStack(spacing: 8) {
            // Minus button
            Button {
                viewModel.setReferencePitch(viewModel.referencePitch - 1)
            } label: {
                Image(systemName: "minus")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(AppColors.accent)
                    .frame(width: 40, height: 40)
            }
            .disabled(!canDecreasePitch(viewModel.referencePitch))
            .accessibilityLabel(String(localized: "reference_pitch_label") + " -1")

            // A=NNN label
            Text("A=\(viewModel.referencePitch)")
                .font(.body.weight(.bold))
                .foregroundColor(AppColors.accent)
                .multilineTextAlignment(.center)
                .frame(minWidth: 60)
                .accessibilityLabel(String(localized: "reference_pitch_label") + " \(viewModel.referencePitch)")

            // Plus button
            Button {
                viewModel.setReferencePitch(viewModel.referencePitch + 1)
            } label: {
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(AppColors.accent)
                    .frame(width: 40, height: 40)
            }
            .disabled(!canIncreasePitch(viewModel.referencePitch))
            .accessibilityLabel(String(localized: "reference_pitch_label") + " +1")
        }
        .frame(maxWidth: .infinity)
        .frame(height: 48)
    }
}
