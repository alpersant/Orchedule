import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../app/providers.dart';
import '../../data/datasources/competition_remote_data_source.dart';
import '../../data/repositories/competition_repository_impl.dart';
import '../../domain/usecases/competition_usecases.dart';
import '../controllers/competition_list_controller.dart';
import 'competition_list_page.dart';

class CompetitionsPage extends ConsumerStatefulWidget {
  const CompetitionsPage({
    super.key,
  });

  @override
  ConsumerState<CompetitionsPage> createState() =>
      _CompetitionsPageState();
}

class _CompetitionsPageState extends ConsumerState<CompetitionsPage> {
  late final CompetitionListController _controller;

  @override
  void initState() {
    super.initState();

    final dio = ref.read(dioProvider);

    final remoteDataSource = CompetitionRemoteDataSourceImpl(dio);

    final repository = CompetitionRepositoryImpl(remoteDataSource);

    final useCases = CompetitionUseCases(repository);

    _controller = CompetitionListController(useCases);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return CompetitionListPage(
      controller: _controller,
    );
  }
}