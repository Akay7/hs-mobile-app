import shared
import SwiftUI

final class StepQuizStringAssembly: StepQuizChildQuizAssembly {
    var moduleInput: StepQuizChildQuizInputProtocol? {
        didSet {
            onModuleInputDidSet(moduleInput)
        }
    }
    weak var moduleOutput: StepQuizChildQuizOutputProtocol?

    private let onModuleInputDidSet: (StepQuizChildQuizInputProtocol?) -> Void

    private let step: Step
    private let dataset: Dataset
    private let reply: Reply?

    private var dataType = StepQuizStringDataType.string

    init(
        dataType: StepQuizStringDataType,
        step: Step,
        dataset: Dataset,
        reply: Reply?,
        onModuleInputDidSet: @escaping (StepQuizChildQuizInputProtocol?) -> Void,
        moduleOutput: StepQuizChildQuizOutputProtocol?
    ) {
        self.dataType = dataType
        self.step = step
        self.dataset = dataset
        self.reply = reply
        self.onModuleInputDidSet = onModuleInputDidSet
        self.moduleOutput = moduleOutput
    }

    init(
        step: Step,
        dataset: Dataset,
        reply: Reply?,
        onModuleInputDidSet: @escaping (StepQuizChildQuizInputProtocol?) -> Void,
        moduleOutput: StepQuizChildQuizOutputProtocol?
    ) {
        self.step = step
        self.dataset = dataset
        self.reply = reply
        self.onModuleInputDidSet = onModuleInputDidSet
        self.moduleOutput = moduleOutput
    }

    func makeModule() -> StepQuizStringView {
        let viewModel = StepQuizStringViewModel(dataType: dataType, dataset: dataset, reply: reply)

        moduleInput = viewModel
        viewModel.moduleOutput = moduleOutput

        return StepQuizStringView(viewModel: viewModel)
    }
}

#if DEBUG
extension StepQuizStringAssembly {
    static func makePlaceholder(dataType: StepQuizStringDataType) -> StepQuizStringAssembly {
        StepQuizStringAssembly(
            dataType: dataType,
            step: .init(),
            dataset: .init(),
            reply: nil,
            onModuleInputDidSet: { _ in },
            moduleOutput: nil
        )
    }
}
#endif