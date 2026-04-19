package com.sample.demo1.ui.counter

import com.sample.demo1.domain.counter.Counter

/**
 * Counter 画面の UI State。
 *
 * Google Android Architecture ガイドに倣い sealed interface で表現する。
 * 画面は一度に 1 つの状態しか取れない（= ローディング中かロード済みかが排他）ことを
 * 型レベルで強制することで、UDF の本質を構造として保証する。
 *
 * Loaded は Int を直接持たず、ドメインモデル [Counter] を保持する。
 * これにより「UI はデータモデルで操作する」(GAA §0.1) を徹底する。
 */
sealed interface CounterUiState {
    /** Repository の初回値が届く前の状態。 */
    data object Loading : CounterUiState

    /** Repository からカウンター値が取得できた状態。 */
    data class Loaded(val counter: Counter) : CounterUiState
}