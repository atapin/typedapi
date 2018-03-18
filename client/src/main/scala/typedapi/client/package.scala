package typedapi

import typedapi.shared._
import shapeless._

package object client extends typedapi.shared.ops.ApiListOps 
                      with HListToCompositionLowPrio 
                      with TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer 
                      with ApiCompilerMediumPrio 
                      with ApiCompilerListLowPrio 
                      with ops.ApiCompilerOps
                      with FoldResultEvidenceLowPrio {

  type Transformed[El <: HList, In <: HList, Out, D <: HList] = (El, In, Out)

  def compile[H <: HList, Fold, El <: HList, In <: HList, Out, D <: HList](apiList: FinalCons[H])
                                                                          (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Fold],
                                                                                    ev: FoldResultEvidence.Aux[Fold, El, In, Out],
                                                                                    compiler: ApiCompiler.Aux[El, In, Out, D]): ApiCompiler.Aux[El, In, Out, D] = 
    compiler

  def compile[H <: HList, In <: HList, Fold <: HList, HL <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                                                (implicit folders: TypeLevelFoldLeftList.Aux[H, In, Fold],
                                                                                          compilers: ApiCompilerList.Aux[Fold, HL], 
                                                                                          composition: HListToComposition[HL]): composition.Out =
    composition(compilers.compilers)
}
